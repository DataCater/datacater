import multiply.transform as transform
import pytest


def test_multiply_int_by_int():
    assert transform.transform(5, {}, {"value": 3}) == 15


def test_multiply_int_by_int_string():
    assert transform.transform(5, {}, {"value": "3"}) == 15


def test_multiply_int_by_float():
    assert transform.transform(5, {}, {"value": 3.0}) == 15


def test_multiply_float_by_float():
    assert round(transform.transform(2.0, {}, {"value": 3.2}), 1) == 6.4


def test_multiply_float_by_float_string():
    assert round(transform.transform(2.0, {}, {"value": "3.2"}), 1) == 6.4


def test_multiply_float_by_int():
    assert round(transform.transform(2.1, {}, {"value": 3}), 1) == 6.3


def test_null_config():
    assert transform.transform(15, {}, {"value": None}) == 15


def test_no_config():
    assert transform.transform(15, {}, {}) == 15
