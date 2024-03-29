# Start app: $ python3 -m uvicorn runner:app --log-level critical
import copy
import datetime
import json
import re
import sys
import traceback
import yaml

from glob import iglob
from importlib import import_module
from os import path

from typing import List, Optional, Any
from fastapi import FastAPI, Response, status, Request
from fastapi.responses import JSONResponse
from pydantic import BaseModel

FILTERS = {}
TRANSFORMS = {}

print("Loading filters from directory 'filters/**' ...")
for filter_spec_file in iglob("./filters/**/spec.y*ml", recursive=True):
    if path.isfile(filter_spec_file):
        filter_dir = path.dirname(filter_spec_file)
        filter_def_file = filter_dir + "/filter.py"
        with open(filter_spec_file, "r") as filter_spec_yaml:
            filter_spec = yaml.safe_load(filter_spec_yaml)
            filter_name = filter_spec["key"]
            module_dir = (
                filter_dir.replace("./filters", "filters").replace("/", ".") + ".filter"
            )
            filter_module = import_module(module_dir)
            FILTERS[filter_name] = getattr(filter_module, "filter")
            print("  • Added filter '" + filter_name + "'")
print("✅ Loaded " + str(len(FILTERS.keys())) + " filters")

print("Loading transforms from directory 'transforms/**' ...")
for transform_spec_file in iglob("./transforms/**/spec.y*ml", recursive=True):
    if path.isfile(transform_spec_file):
        transform_dir = path.dirname(transform_spec_file)
        transform_def_file = transform_dir + "/transform.py"
        with open(transform_spec_file, "r") as transform_spec_yaml:
            transform_spec = yaml.safe_load(transform_spec_yaml)
            transform_name = transform_spec["key"]
            module_dir = (
                transform_dir.replace("./transforms", "transforms").replace("/", ".")
                + ".transform"
            )
            transform_module = import_module(module_dir)
            TRANSFORMS[transform_name] = getattr(transform_module, "transform")
            print("  • Added transform '" + transform_name + "'")
print("✅ Loaded " + str(len(TRANSFORMS.keys())) + " transforms")

# Apply the following transforms, which change the schema of a record,
# at the end of a transformation step
INTERNAL_POST_TRANSFORMS = ["cast-data-type", "drop-field", "new-field", "rename-field"]


class PreviewRequest(BaseModel):
    records: List[dict]
    pipeline: dict
    previewStep: Optional[int]


class PipelineRequest(BaseModel):
    spec: dict


app = FastAPI()

pipeline = {"spec": {"steps": []}}

# Load pipeline spec from config map
CONFIG_MAP_FILE = "/usr/app/mounts/spec"
if path.isfile(CONFIG_MAP_FILE):
    config_map = open(CONFIG_MAP_FILE, "r")
    pipeline["spec"] = json.loads(config_map.read())
    config_map.close()


def apply_pipeline(record: dict, pipeline: dict):
    location = {}
    try:
        step_index = 0
        for step in pipeline["spec"]["steps"]:
            if step["kind"] == "Record":
                location["path"] = "steps[{}]".format(step_index)

                record_filter = step.get("filter")
                record_transform = step.get("transform")

                record_matches_filter = True
                if record_filter is not None and record_filter.get("key") is not None:
                    record_matches_filter = FILTERS[record_filter["key"]](
                        record, record_filter.get("config", {})
                    )
                    if record_matches_filter is False and (
                        record_transform is None or record_transform.get("key") is None
                    ):
                        return None

                # Apply transform only if no filter is defined or record matches filter
                if (
                    record_matches_filter
                    and record_transform is not None
                    and record_transform.get("key") is not None
                ):
                    record = TRANSFORMS[record_transform["key"]](
                        record, record_transform.get("config", {})
                    )
            else:  # step["kind"] == "Field"
                value = record["value"]
                for field_name, field_config in step["fields"].items():
                    location["path"] = "steps[{}].fields[{}]".format(
                        step_index, field_name
                    )

                    field_filter = field_config.get("filter")
                    field_transform = field_config.get("transform")

                    if (
                        field_transform is None
                        or field_transform.get("key") not in INTERNAL_POST_TRANSFORMS
                    ):  # noqa: E501
                        field_matches_filter = True
                        if (
                            field_filter is not None
                            and field_filter.get("key") is not None
                        ):
                            field_matches_filter = FILTERS[field_filter["key"]](
                                value.get(field_name, None),
                                value,
                                field_filter.get("config", {}),
                            )
                            if field_matches_filter is False and (
                                field_transform is None
                                or field_transform.get("key") is None
                            ):
                                return None

                        # Apply transform only if no filter is defined or field matches filter
                        if (
                            field_matches_filter
                            and field_transform is not None
                            and field_transform.get("key") is not None
                        ):
                            value[field_name] = TRANSFORMS[field_transform["key"]](
                                value.get(field_name, None),
                                value,
                                field_transform.get("config", {}),
                            )

                # Apply internal post transforms
                for field_name, field_config in step["fields"].items():
                    location["path"] = "steps[{}].fields[{}]".format(
                        step_index, field_name
                    )

                    field_filter = field_config.get("filter")
                    field_transform = field_config.get("transform")

                    if (
                        field_transform is not None
                        and field_transform.get("key") in INTERNAL_POST_TRANSFORMS
                    ):  # noqa: E501
                        transform_key = field_transform.get("key")
                        field_matches_filter = True
                        if (
                            field_filter is not None
                            and field_filter.get("key") is not None
                        ):
                            field_matches_filter = FILTERS[field_filter["key"]](
                                value.get(field_name, None),
                                value,
                                field_filter.get("config", {}),
                            )

                        # Apply transform only if no filter is defined or field matches filter
                        if field_matches_filter:
                            if transform_key == "drop-field":
                                value.pop(field_name)
                            elif transform_key == "rename-field":
                                new_name = field_transform.get("config", {})[
                                    "newFieldName"
                                ]
                                value[new_name] = value.pop(field_name)
                            else:
                                value[field_name] = TRANSFORMS[field_transform["key"]](
                                    value.get(field_name, None),
                                    value,
                                    field_transform.get("config", {}),
                                )
                record["value"] = value

            step_index = step_index + 1
        return record
    except:  # noqa: E722
        return add_error_to_metadata(record, location, sys.exc_info()[0], traceback)


def preview_pipeline(record: dict, pipeline: dict, preview_step=None):
    location = {}
    try:
        step_index = 0
        for step in pipeline["spec"]["steps"]:
            # Remember state of record before applying step
            old_record = copy.deepcopy(record)
            if step["kind"] == "Record":
                location["path"] = "steps[{}]".format(step_index)

                record_filter = step.get("filter")
                record_transform = step.get("transform")

                record_matches_filter = True
                if record_filter is not None and record_filter.get("key") is not None:
                    record_matches_filter = FILTERS[record_filter["key"]](
                        record, record_filter.get("config", {})
                    )
                    if record_matches_filter is False and (
                        record_transform is None or record_transform.get("key") is None
                    ):
                        return add_filtered_out_at_step(
                            record, step_index, preview_step
                        )

                # Apply transform only if no filter is defined or record matches filter
                if (
                    record_matches_filter
                    and record_transform is not None
                    and record_transform.get("key") is not None
                ):
                    record = TRANSFORMS[record_transform["key"]](
                        record, record_transform.get("config", {})
                    )
            else:  # step["kind"] == "Field"
                value = record["value"]
                for field_name, field_config in step["fields"].items():
                    location["path"] = "steps[{}].fields[{}]".format(
                        step_index, field_name
                    )

                    field_filter = field_config.get("filter")
                    field_transform = field_config.get("transform")

                    if (
                        field_transform is None
                        or field_transform.get("key") not in INTERNAL_POST_TRANSFORMS
                    ):  # noqa: E501
                        field_matches_filter = True
                        if (
                            field_filter is not None
                            and field_filter.get("key") is not None
                        ):
                            field_matches_filter = FILTERS[field_filter["key"]](
                                value.get(field_name, None),
                                value,
                                field_filter.get("config", {}),
                            )
                            if field_matches_filter is False and (
                                field_transform is None
                                or field_transform.get("key") is None
                            ):
                                return add_filtered_out_at_step(
                                    record, step_index, preview_step
                                )

                        # Apply transform only if no filter is defined or field matches filter
                        if (
                            field_matches_filter
                            and field_transform is not None
                            and field_transform.get("key") is not None
                        ):
                            value[field_name] = TRANSFORMS[field_transform["key"]](
                                value.get(field_name, None),
                                value,
                                field_transform.get("config", {}),
                            )

                # Apply internal post transforms
                for field_name, field_config in step["fields"].items():
                    location["path"] = "steps[{}].fields[{}]".format(
                        step_index, field_name
                    )

                    field_filter = field_config.get("filter")
                    field_transform = field_config.get("transform")

                    if (
                        field_transform is not None
                        and field_transform.get("key") in INTERNAL_POST_TRANSFORMS
                    ):  # noqa: E501
                        transform_key = field_transform.get("key")
                        field_matches_filter = True
                        if (
                            field_filter is not None
                            and field_filter.get("key") is not None
                        ):
                            field_matches_filter = FILTERS[field_filter["key"]](
                                value.get(field_name, None),
                                value,
                                field_filter.get("config", {}),
                            )

                        # Apply transform only if no filter is defined or field matches filter
                        if field_matches_filter:
                            if transform_key == "drop-field":
                                value.pop(field_name)
                            elif transform_key == "rename-field":
                                new_name = field_transform.get("config", {})[
                                    "newFieldName"
                                ]
                                value[new_name] = value.pop(field_name)
                            else:
                                value[field_name] = TRANSFORMS[field_transform["key"]](
                                    value.get(field_name, None),
                                    value,
                                    field_transform.get("config", {}),
                                )
                record["value"] = value

            # Initialize metadata keeping track of applied changes
            if record["metadata"].get("lastChange") is None:
                record["metadata"]["lastChange"] = {"key": {}, "value": {}}
            # Check which fields of the key have been changed by this step
            if type(record["key"]) is dict:
                for key in record["key"].keys():
                    if old_record["key"] is None or record["key"].get(
                        key
                    ) != old_record["key"].get(key):
                        record["metadata"]["lastChange"]["key"][key] = step_index
            # Check which fields of the value have been changed by this step
            if type(record["value"]) is dict:
                for key in record["value"].keys():
                    if old_record["value"] is None or record["value"].get(
                        key
                    ) != old_record["value"].get(key):
                        record["metadata"]["lastChange"]["value"][key] = step_index

            if preview_step == step_index:
                return record

            step_index = step_index + 1
        return record
    except:  # noqa: E722
        return add_error_to_metadata(record, location, sys.exc_info()[0], traceback)


def add_error_to_metadata(record, location, error_type, traceback):
    file_name = None
    line_number = None
    formatted_error = traceback.format_exc()
    formatted_errors = formatted_error.split("\n")
    if len(formatted_errors) > 3:
        file_line_error = formatted_errors[-4].strip()
        if match := re.search('File "(.*)".*', file_line_error):
            file_name = match.group(1)
        if match := re.search(".*line ([0-9]*).*", file_line_error):
            line_number = int(match.group(1))
    error_message = None
    if len(formatted_errors) > 1:
        error_message = formatted_errors[-2]

    # inject error information into metadata
    record["metadata"]["error"] = {
        "location": location,
        "exceptionMessage": error_message,
        "exceptionName": error_type.__name__,
        "file": {
            "name": file_name,
            "lineNumber": line_number,
        },
        "stacktrace": formatted_errors,
    }

    return record


def add_filtered_out_at_step(record, step_index, preview_step):
    if preview_step is None or step_index == preview_step:
        record["metadata"] = {"filteredOutAtStep": step_index}
        return record
    else:
        return None


@app.post("/batch")
async def apply_to_multiple_records(records: List[dict], response: Response):
    processed_records = []
    for record in records:
        processed_record = apply_pipeline(record, pipeline)
        if processed_record is not None:
            processed_records.append(processed_record)
    return processed_records


@app.post("/batch-file")
async def apply_to_multiple_records_file(request: dict, response: Response):
    temp_file = str(request["fileIn"])
    with open(temp_file) as json_file:
        records = json.load(json_file)
        processed_records = []
        for record in records:
            processed_record = apply_pipeline(record, pipeline)
            if processed_record is not None:
                processed_records.append(processed_record)
        with open(temp_file + ".out", "w") as outfile:
            json.dump(processed_records, outfile)
    return {"fileOut": temp_file + ".out"}


@app.post("/preview")
async def preview(previewRequest: PreviewRequest, response: Response):
    processed_records = []
    for record in previewRequest.records:
        processed_record = preview_pipeline(
            record, previewRequest.pipeline, previewRequest.previewStep
        )
        if processed_record is not None:
            processed_records.append(processed_record)
    return processed_records


@app.get("/health")
async def health():
    try:
        status_ok = {"status": 200, "message": "All systems are UP and running!"}
        return status_ok
    except:  # noqa: E722
        return {
            "status_code": 500,
            "message": "We could not process your request "
            "due to an internal server error!",
        }


@app.post("/pipeline")
async def store_pipeline(request: PipelineRequest, response: Response):
    global pipeline
    pipeline["spec"] = request.spec
