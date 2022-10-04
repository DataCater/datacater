# DataCater Python® runner

This Python tool is used for evaluating our filters and transforms.

The Python runner is being deployed as a sidecar container in the pod of the Quarkus-based
streaming pipelines and called via HTTP. We are currently considering
switching to gRPC for performance reasons.

## Try it out the Python runner outside of DataCater

Install Python dependencies:

```
$ pip3 install -r requirements.txt
```

Start the HTTP server:

```
$ python3 -m uvicorn runner:app
```

The pipeline runner automatically loads the `example.pipeline.yaml` from
this directory.

Send a request:

```
$ curl http://localhost:8000/batch \
  -XPOST \
  -H'Content-Type:application/json'
  -d'
  [{
    "key": null,
    "value": { "name": "Max Mustermann", "email": "max-mustermann@datacater.io", "is_admin": true },
    "metadata": {}
  }]
  '
```

## How to build the docker image locally?
```
bash build.sh

docker run -it --rm --name python-runner-local python-runner bash

# To run the server interactively
python3 -m uvicorn runner:app --port 50000 --host 0.0.0.0

# To run the health checks
python3 -m uvicorn runner:app --port 50000 --host 0.0.0.0 &
python3 health_check.py
```
