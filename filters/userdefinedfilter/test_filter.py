import userdefinedfilter.filter as filter
import pytest

def test_udf_returns_true():
    assert filter.filter(
        42,
        {},
        { "code": "def filter(value,row):\n  return value == 42" }
    ) is True

def test_udf_returns_false():
    assert filter.filter(
        "42",
        {},
        { "code": "def filter(value,row):\n  return value == 42" }
    ) is False

def test_udf_with_wrong_function_name_throws_exception():
    with pytest.raises(KeyError):
        filter.filter(
            "42",
            {},
            { "code": "def filterfoo(value,row):\n  return value == 42" }
        )

def test_udf_with_wrong_parameters_throws_exception():
    with pytest.raises(TypeError):
        filter.filter(
            "42",
            {},
            { "code": "def filter(value):\n  return value == 42" }
        )

def test_no_config_returns_true():
    assert filter.filter(
        "42",
        {},
        {}
    ) is True
