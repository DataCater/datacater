import notequal.filter as filter
import pytest

def test_string_does_not_equal_string():
    assert filter.filter(
        "DataCater is the developer-friendly ETL platform for transforming data in motion.",
        {},
        { "value": "DataCater is the developer-unfriendly ETL platform for transforming data in motion." }
    ) is True

def test_string_equals_string():
    assert filter.filter(
        "DataCater is the developer-friendly ETL platform for transforming data in motion.",
        {},
        { "value": "DataCater is the developer-friendly ETL platform for transforming data in motion." }
    ) is False

def test_string_does_not_equal_number():
    assert filter.filter(
        "42",
        {},
        { "value": 42 }
    ) is True

def test_number_does_not_equal_string():
    assert filter.filter(
        42,
        {},
        { "value": "42" }
    ) is True

def test_boolean_does_not_equal_string():
    assert filter.filter(
        True,
        {},
        { "value": "True" }
    ) is True

def test_float_equals_int():
    assert filter.filter(
        42.0,
        {},
        { "value": 42 }
    ) is False

def test_int_equals_float():
    assert filter.filter(
        42,
        {},
        { "value": 42.0 }
    ) is False

def test_empty_string_does_not_equal_none():
    assert filter.filter(
        "",
        {},
        { "value": None }
    ) is True

def no_config():
    with pytest.raises(KeyError):
        filter.filter(
            42,
            {},
            {}
        )
