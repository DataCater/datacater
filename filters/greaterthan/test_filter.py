import greaterthan.filter as filter
import pytest


def test_string_is_greater_than_other_string():
    assert filter.filter("Python", {}, {"value": "Php"})


def test_string_is_greater_than_number_raises_exception():
    with pytest.raises(TypeError):
        filter.filter("5", {}, {"value": 4})


def test_number_is_greater_than_other_number():
    assert filter.filter(5, {}, {"value": 4})


def test_int_is_greater_than_float():
    assert filter.filter(5, {}, {"value": 4.0})


def test_float_is_greater_than_int():
    assert filter.filter(5.0, {}, {"value": 4})


def no_config():
    with pytest.raises(KeyError):
        filter.filter(42, {}, {})
