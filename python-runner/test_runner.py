# Run tests: $ python3 -m pytest

from fastapi.testclient import TestClient

from runner import app

client = TestClient(app)

def test_preview_apply_transform():
    response = client.post("/preview", json = {
        "pipeline": {
            "spec": {
                "steps": [
                    {
                        "kind": "Field",
                        "fields": {
                            "company": {
                                "transform": {
                                    "key": "trim"
                                }
                            }
                        }
                    }
                ]
            }
        },
        "records": [
            {
                "key": {},
                "value": {
                    "company": " DataCater GmbH     "
                },
                "metadata": {}
            }
        ]
    })

    assert response.json() == [{
        "key": {},
        "value": {
            "company": "DataCater GmbH"
        },
        "metadata": {}
    }]

    assert response.status_code == 200
