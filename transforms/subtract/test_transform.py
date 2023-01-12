import subtract.transform as transform
import pytest


def test_subtract_int_from_int():
    assert transform.transform(15, {}, {"value": 3}) == 12


def test_subtract_int_from_int_string():
    assert transform.transform(15, {}, {"value": "3"}) == 12


def test_subtract_int_from_float():
    assert transform.transform(15.0, {}, {"value": 3}) == 12


def test_subtract_float_from_int():
    assert transform.transform(15, {}, {"value": 3.0}) == 12


def test_subtract_float_from_float():
    assert round(transform.transform(15.3, {}, {"value": 3.1}), 1) == 12.2


def test_subtract_float_from_float_string():
    assert round(transform.transform(15.3, {}, {"value": "3.1"}), 1) == 12.2


def test_null_config():
    assert transform.transform(15, {}, {"value": None}) == 15


def test_no_config():
    assert transform.transform(15, {}, {}) == 15
