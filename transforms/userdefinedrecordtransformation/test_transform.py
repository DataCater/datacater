import userdefinedrecordtransformation.transform as transform
import copy
import pytest

sample_record = {
    "key": {"id": 42},
    "value": {"name": "DataCater", "url": "http://datacater.io"},
    "metadata": {},
}


def test_udf_change_value():
    assert transform.transform(
        copy.deepcopy(sample_record),
        {
            "code": 'def transform(record):\n  record["value"]["url"] = record["value"]["url"].replace("http", "https")\n  return record'
        },
    ) == {
        "key": {"id": 42},
        "value": {"name": "DataCater", "url": "https://datacater.io"},
        "metadata": {},
    }


def test_udf_change_key():
    assert transform.transform(
        copy.deepcopy(sample_record),
        {
            "code": 'def transform(record):\n  record["key"] = record["key"]["id"]\n  return record'
        },
    ) == {
        "key": 42,
        "value": {"name": "DataCater", "url": "http://datacater.io"},
        "metadata": {},
    }


def test_udf_with_wrong_function_name_throws_exception():
    with pytest.raises(KeyError):
        transform.transform(
            sample_record, {"code": "def transformama(record):\n  return record"}
        )


def test_udf_with_wrong_parameters_throws_exception():
    with pytest.raises(TypeError):
        transform.transform({}, {"code": "def transform():\n  return record"})


def test_no_config_returns_value():
    assert transform.transform({}, {}) == {}
    assert transform.transform(copy.deepcopy(sample_record), {}) == sample_record
