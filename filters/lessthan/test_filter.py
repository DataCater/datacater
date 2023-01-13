import lessthan.filter as filter
import pytest


def test_string_is_less_than_other_string():
    assert filter.filter("Python", {}, {"value": "Ruby"})


def test_string_is_less_than_number_raises_exception():
    with pytest.raises(TypeError):
        filter.filter("5", {}, {"value": 6})


def test_number_is_less_than_other_number():
    assert filter.filter(5, {}, {"value": 6})


def test_int_is_less_than_float():
    assert filter.filter(5, {}, {"value": 6.0})


def test_float_is_less_than_int():
    assert filter.filter(5.0, {}, {"value": 6})


def test_int_is_less_than_int_string():
    assert filter.filter(5, {}, {"value": "6"})


def null_config():
    assert filter.filter(42, {}, {}) is False


def no_config():
    assert filter.filter(42, {}, {}) is False
