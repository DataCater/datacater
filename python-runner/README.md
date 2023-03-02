# DataCater Python® runner

The Python runner is used for previewing and running pipelines. It applies pre-defined and inline functions to records.

For Deployments, the Python runner is being deployed as a sidecar container.

## Try out the Python runner outside of DataCater

Install the Python dependencies:

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
  -H'Content-Type:application/json' \
  -d'
  [{
    "key": null,
    "value": { "name": "Max Mustermann", "email": "max-mustermann@datacater.io", "is_admin": true },
    "metadata": {}
  }]
  '
```

## Running tests

After copying the pre-defined [filters](https://github.com/DataCater/datacater/tree/main/filters) and [transforms](https://github.com/DataCater/datacater/tree/main/transforms) from the root directory of this repository, you can invoke `pytest`:

```
$ cp -r ../filters .
$ cp -r ../transforms .
$ python3 -m pytest test_runner.py
```
