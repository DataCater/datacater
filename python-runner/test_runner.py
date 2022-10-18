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

def test_preview_apply_user_defined_transform():
    response = client.post("/preview", json = {
        "pipeline": {
            "spec": {
                "steps": [
                    {
                        "kind": "Field",
                        "fields": {
                            "company": {
                                "transform": {
                                    "key": "user-defined-transformation",
                                    "config": {
                                        "code": "def transform(value, record):\n  return value.replace('GmbH', 'AG')"
                                    }
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
                    "company": "DataCater GmbH"
                },
                "metadata": {}
            }
        ]
    })

    assert response.json() == [{
        "key": {},
        "value": {
            "company": "DataCater AG"
        },
        "metadata": {}
    }]

    assert response.status_code == 200

def test_preview_apply_user_defined_record_transform():
    response = client.post("/preview", json = {
        "pipeline": {
            "spec": {
                "steps": [
                    {
                        "kind": "Record",
                        "transform": {
                            "key": "user-defined-record-transformation",
                            "config": {
                                "code": "def transform(record):\n  record['value']['website'] = 'https://datacater.io'\n  return record"
                            }
                        }
                    },
                    {
                        "kind": "Field",
                        "fields": {
                            "company": {
                                "transform": {
                                    "key": "user-defined-transformation",
                                    "config": {
                                        "code": "def transform(value, record):\n  return value.replace('GmbH', 'AG')"
                                    }
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
                    "company": "DataCater GmbH"
                },
                "metadata": {}
            }
        ]
    })

    assert response.json() == [{
        "key": {},
        "value": {
            "company": "DataCater AG",
            "website": "https://datacater.io"
        },
        "metadata": {}
    }]

    assert response.status_code == 200

def test_preview_apply_filter():
    response = client.post("/preview", json = {
        "pipeline": {
            "spec": {
                "steps": [
                    {
                        "kind": "Field",
                        "fields": {
                            "company": {
                                "filter": {
                                    "key": "contain",
                                    "config": {
                                        "value": "Cat"
                                    }
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
                    "company": "DataCater GmbH"
                },
                "metadata": {}
            },
            {
                "key": {},
                "value": {
                    "company": "DataKater GmbH"
                },
                "metadata": {}
            },
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

def test_preview_apply_user_defined_filter():
    response = client.post("/preview", json = {
        "pipeline": {
            "spec": {
                "steps": [
                    {
                        "kind": "Field",
                        "fields": {
                            "company": {
                                "filter": {
                                    "key": "user-defined-filter",
                                    "config": {
                                        "code": "def filter(value, row):\n  return value.startswith('DataKater')"
                                    }
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
                    "company": "DataCater GmbH"
                },
                "metadata": {}
            },
            {
                "key": {},
                "value": {
                    "company": "DataKater GmbH"
                },
                "metadata": {}
            },
        ]
    })

    assert response.json() == [{
        "key": {},
        "value": {
            "company": "DataKater GmbH"
        },
        "metadata": {}
    }]

    assert response.status_code == 200

def test_preview_apply_user_defined_record_filter():
    response = client.post("/preview", json = {
        "pipeline": {
            "spec": {
                "steps": [
                    {
                        "kind": "Field",
                        "fields": {
                            "company": {
                                "transform": {
                                    "key": "user-defined-transformation",
                                    "config": {
                                        "code": "def transform(value, record):\n  return value.replace('GmbH', 'AG')"
                                    }
                                }
                            }
                        }
                    },
                    {
                        "kind": "Record",
                        "filter": {
                            "key": "user-defined-record-filter",
                            "config": {
                                "code": "def filter(record):\n  return record['value']['company'].endswith('AG')"
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
                    "company": "DataCater GmbH"
                },
                "metadata": {}
            }
        ]
    })

    assert response.json() == [{
        "key": {},
        "value": {
            "company": "DataCater AG"
        },
        "metadata": {}
    }]

    assert response.status_code == 200

def test_preview_filter_transform_combination():
    response = client.post("/preview", json = {
        "pipeline": {
            "spec": {
                "steps": [
                    {
                        "kind": "Field",
                        "fields": {
                            "company": {
                                "transform": {
                                    "key": "user-defined-transformation",
                                    "config": {
                                        "code": "def transform(value, record):\n  return value.replace('GmbH', 'AG')"
                                    }
                                },
                                "filter": {
                                    "key": "user-defined-filter",
                                    "config": {
                                        "code": "def filter(value, record):\n  return 'Cat' in value"
                                    }
                                },
                            }
                        }
                    },
                ]
            }
        },
        "records": [
            {
                "key": {},
                "value": {
                    "company": "DataCater GmbH"
                },
                "metadata": {}
            },
            {
                "key": {},
                "value": {
                    "company": "DataKater GmbH"
                },
                "metadata": {}
            }
        ]
    })

    assert response.json() == [
        {
            "key": {},
            "value": {
                "company": "DataCater AG"
            },
            "metadata": {}
        },
        {
            "key": {},
            "value": {
                "company": "DataKater GmbH"
            },
            "metadata": {}
        }
    ]

    assert response.status_code == 200
