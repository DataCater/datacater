import dividefield.transform as transform
import pytest


def test_divide_int_by_int():
    assert transform.transform(15, {"number": 3}, {"fieldName": "number"}) == 5


def test_divide_int_by_float():
    assert transform.transform(15, {"number": 3.0}, {"fieldName": "number"}) == 5


def test_divide_float_by_float():
    assert transform.transform(6.3, {"number": 2.1}, {"fieldName": "number"}) == 3.0


def test_divide_float_by_int():
    assert transform.transform(6.3, {"number": 2}, {"fieldName": "number"}) == 3.15


def test_field_does_not_exist():
    with pytest.raises(KeyError):
        transform.transform(15, {}, {"fieldName": "number"})


def test_no_config():
    with pytest.raises(KeyError):
        transform.transform(15, {}, {})
