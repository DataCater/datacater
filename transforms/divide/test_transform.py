import divide.transform as transform
import pytest


def test_divide_int_by_int():
    assert transform.transform(15, {}, {"value": 3}) == 5


def test_divide_int_by_int_string():
    assert transform.transform(15, {}, {"value": "3"}) == 5


def test_divide_int_by_float():
    assert transform.transform(15, {}, {"value": 3.0}) == 5


def test_divide_int_by_float():
    assert transform.transform(15, {}, {"value": 3.0}) == 5


def test_divide_float_by_float():
    assert transform.transform(6.3, {}, {"value": 2.1}) == 3.0


def test_divide_float_by_float_string():
    assert transform.transform(6.3, {}, {"value": "2.1"}) == 3.0


def test_divide_float_by_int():
    assert transform.transform(6.3, {}, {"value": 2}) == 3.15


def test_null_config():
    assert transform.transform(15, {}, {"value": None}) == 15


def test_no_config():
    assert transform.transform(15, {}, {}) == 15
