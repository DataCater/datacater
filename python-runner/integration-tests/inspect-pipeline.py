import json

import requests as re
from kafka import KafkaConsumer, KafkaProducer


url = "http://localhost:8080/api/alpha"
streams = "/streams"
pipelines = "/pipelines"


def create_stream() -> json:
    stream = {
        "name": "test-topic",
        "spec": {
            "kafka": {
                "topic": {
                    "config": {},
                    "num.partitions": "10",
                    "replication.factor": "3"
                },
                "bootstrap.servers": "pkc-75m1o.europe-west3.gcp.confluent.cloud:9092",
                "security.protocol": "SASL_SSL",
                "sasl.jaas.config": "org.apache.kafka.common.security.plain.PlainLoginModule   required username='Y7VORDDOSQUDTFWO'   password='ANfOKwxFTWj1DqESIdbauc0ailNiQOMto3UHeEj9DG376JJgHDp58czEhgWKNP6L';",
                "sasl.mechanism": "PLAIN",
                "client.dns.lookup": "use_all_dns_ips",
                "acks": "all",
                "session.timeout.ms": "45000"
            },
            "kind": "KAFKA"
        }
    }

    url_streams = url + streams
    return re.post(url=url_streams, data=json.dumps(stream)).json()


def create_pipeline_with_stream(uuid: str):
    pipeline = {
        "name": "pipeline-test",
        "apiVersion": "datacater.io/v1",
        "kind": "Pipeline",
        "metadata": {
            "stream-in": uuid
        },
        "spec": {
            "filters": [],
            "transformations": []
        }
    }

    url_pipelines = url + pipelines
    return re.post(url=url_pipelines, data=json.dumps(pipeline)).json()


def inspect_pipeline(uuid: str):
    url_inspect_pipeline = url + pipelines + uuid + "/inspect"
    return re.get(url_inspect_pipeline).json()


### Create a stream, create pipeline, with attached stream, finally inspect
def inspect_pipeline():
    created_stream = create_stream()
    stream_uuid = created_stream["uuid"]
    created_pipeline = create_pipeline_with_stream(stream_uuid)
    pipeline_uuid = created_pipeline["uuid"]
    inspect_url = url + pipelines + "/" + pipeline_uuid + "/inspect"
    return re.get(inspect_url).json()


def inspect_stream(uuid: str):
    url_inspect_stream = url + streams + "/" + uuid + "/inspect"
    return re.get(url_inspect_stream)


def consume_topic(topic: str):
    consumer = KafkaConsumer(topic)
    for msg in consumer:
        print(msg)

if "__main__" == __name__:
   inspection = inspect_pipeline()
   print(inspection)
