# start APP: $ python3 -m uvicorn runner:app --log-level critical

import re
import sys
import traceback
from glob import iglob
from importlib import import_module
from os import path
from typing import List, Optional

import yaml
from fastapi import FastAPI, Response, status
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
            module_dir = filter_dir.replace('./filters', 'filters').replace(
                '/', '.') + '.filter'
            filter_module = import_module(module_dir)
            FILTERS[filter_name] = getattr(filter_module, 'filter')
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
            module_dir = \
                transform_dir.replace('./transforms', 'transforms')\
                             .replace('/', '.') + '.transform'
            transform_module = import_module(module_dir)
            TRANSFORMS[transform_name] = getattr(transform_module, 'transform')
            print("  • Added transform '" + transform_name + "'")
print("✅ Loaded " + str(len(TRANSFORMS.keys())) + " transforms")

# Apply the following transforms, which change the schema of a record,
# at the end of a transformation step
INTERNAL_POST_TRANSFORMS = [
    "add-column",
    "cast-data-type",
    "drop-column",
    "rename-column"
]

class Record(BaseModel):
    key: Optional[dict]
    value: dict
    metadata: dict

class PreviewRequest(BaseModel):
    records: List[Record]
    pipeline: dict
    previewStage: Optional[str]
    previewStep: Optional[int]

class PipelineRequest(BaseModel):
    # payload describes the whole expected schema of
    # a pipeline including:
    # apiVersion: str
    # kind: str
    # metadata: dict
    # spec: dict
    payload: dict

class PipelineRequest(BaseModel):
    spec: dict

app = FastAPI()

pipeline = {
    "spec": {
        "filters": [],
        "transformationSteps": []
    }
}

def apply_pipeline(record: Record, pipeline: dict, previewStage = None, previewStep = None):
    location = {}
    try:
        # For the time being, we only operate on values of records
        value = record.value
        # return None if one of the filters fails
        filter_index = 0
        for filter in pipeline["spec"]["filters"]:
            location["path"] = "filters[{}]".format(filter_index)
            if FILTERS[filter["filter"]](value[filter["attributeName"]], value,
                                         filter.get("filterConfig", {})) is False:
                return None
            filter_index = filter_index + 1

        if previewStage == "filter":
            return record

        step_index = 0
        for transformation_step in pipeline["spec"]["transformationSteps"]:
            # Split transforms into "Regular" and internal "Post" transforms
            # Apply regular transforms
            transformation_index = 0
            for transformation in transformation_step["transformations"]:
                if transformation["transformation"] not in INTERNAL_POST_TRANSFORMS:  # noqa: E501
                    location["path"] = "transformationSteps[{}][{}]".format(
                        step_index, transformation_index)

                    can_apply_transformation = True
                    filter = transformation.get("filter","")
                    if filter is not None and len(filter) > 0:
                        can_apply_transformation = FILTERS[
                            transformation["filter"]](
                            value[transformation["attributeName"]], value,
                            transformation.get("filterConfig", {}))

                    if can_apply_transformation:
                        value[transformation["attributeName"]] = TRANSFORMS[
                            transformation["transformation"]](
                            value.get(transformation["attributeName"], None),
                            value,
                            transformation.get("transformationConfig", {}))
                transformation_index = transformation_index + 1

            # Apply post transforms
            transformation_index = 0
            for transformation in transformation_step["transformations"]:
                if transformation["transformation"] in INTERNAL_POST_TRANSFORMS:
                    location["path"] = "transformationSteps[{}][{}]".format(
                        step_index, transformation_index)

                    can_apply_transformation = True
                    if "filter" in transformation:
                        can_apply_transformation = FILTERS[
                            transformation["filter"]](
                            value[transformation["attributeName"]], value,
                            transformation.get("filterConfig", {}))

                    if can_apply_transformation:
                        if transformation["transformation"] == "drop-column":
                            value.pop(transformation["attributeName"])
                        elif transformation["transformation"] == "rename-column":
                            old_name = transformation["attributeName"]
                            new_name = transformation["transformationConfig"][
                                "newAttributeName"]
                            value[new_name] = value.pop(old_name)
                        else:
                            value[transformation["attributeName"]] = TRANSFORMS[
                                transformation["transformation"]](
                                value.get(transformation["attributeName"], None),
                                value,
                                transformation.get("transformationConfig", {}))
                transformation_index = transformation_index + 1
            if previewStage == "transform" and previewStep == step_index:
                record.value = value
                return record

            step_index = step_index + 1

        record.value = value
        return record
    except: #noqa: E722
        error_type = sys.exc_info()[0]
        file_name = None
        line_number = None
        formatted_error = traceback.format_exc()
        formatted_errors = formatted_error.split("\n")
        if len(formatted_errors) > 3:
            file_line_error = formatted_errors[-4].strip()
            if match := re.search('File "(.*)".*', file_line_error):
                file_name = match.group(1)
            if match := re.search('.*line ([0-9]*).*', file_line_error):
                line_number = int(match.group(1))
        error_message = None
        if len(formatted_errors) > 1:
            error_message = formatted_errors[-2]

        # inject error information into metadata
        record.metadata["error"] = {
            "location": location,
            "exceptionMessage": error_message,
            "exceptionName": error_type.__name__,
            "file": {
                "name": file_name,
                "lineNumber": line_number,
            },
            "stacktrace": formatted_errors
        }

        return record

@app.post("/", response_model=Record)
async def apply_to_single_record(record: Record):
    return apply_pipeline(record, pipeline)

@app.post("/batch", response_model=List[Record])
async def apply_to_multiple_records(records: List[Record], response: Response):
    processed_records = []
    for record in records:
        processed_record = apply_pipeline(record, pipeline)
        if processed_record is not None:
            processed_records.append(record)
            return processed_records

@app.post("/preview", response_model=List[Record])
async def preview(previewRequest: PreviewRequest, response: Response):
    if previewRequest.previewStage == "explore":
        return previewRequest.records
    processed_records = []
    for record in previewRequest.records:
        processed_record = apply_pipeline(record, previewRequest.pipeline, previewRequest.previewStage, previewRequest.previewStep)
        if processed_record is not None:
            processed_records.append(processed_record)
    return processed_records

@app.get("/health")
async def health():
    try:
        status_ok = {"status": 200,
                     "message": "All systems are UP and running!"}
        return status_ok
    except:  # noqa: E722
        return {
            "status_code": 500,
            "message": "We could not process your request "
                       "due to an internal server error!"
        }

@app.post("/pipeline")
async def store_pipeline(request: PipelineRequest,
                         response: Response):

    global pipeline
    pipeline = request.payload
