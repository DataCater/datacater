import userdefinedtransformation.transform as transform
import pytest


def test_udf():
    assert (
        transform.transform(
            None, {}, {"code": "def transform(value,row):\n  return 42"}
        )
        == 42
    )


def test_udf_object():
    assert (
        transform.transform(
            {"name": "DataCater", "legal": "GmbH"},
            {},
            {
                "code": 'def transform(value,row):\n  return value["name"] + " " + value["legal"]'
            },
        )
        == "DataCater GmbH"
    )


def test_udf_access_row():
    assert (
        transform.transform(
            42,
            {"magicNumber": 100},
            {"code": 'def transform(value,row):\n  return value + row["magicNumber"]'},
        )
        == 142
    )


def test_udf_with_wrong_function_name_throws_exception():
    with pytest.raises(KeyError):
        transform.transform(
            None, {}, {"code": "def transformama(value,row):\n  return 42"}
        )


def test_udf_with_wrong_parameters_throws_exception():
    with pytest.raises(TypeError):
        transform.transform(None, {}, {"code": "def transform(value):\n  return value"})


def test_no_config_returns_value():
    assert transform.transform(None, {}, {}) == None
    assert transform.transform("foo", {}, {}) == "foo"
    assert transform.transform(12, {}, {}) == 12
    assert transform.transform({"name": "DataCater Gmbh"}, {}, {}) == {
        "name": "DataCater Gmbh"
    }
    assert transform.transform(["DataCater GmbH"], {}, {}) == ["DataCater GmbH"]
