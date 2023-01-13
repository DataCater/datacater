import modulo.transform as transform
import pytest


def test_modulo_int_by_int():
    assert transform.transform(14, {}, {"value": 3}) == 2


def test_modulo_int_by_int_string():
    assert transform.transform(14, {}, {"value": "3"}) == 2


def test_modulo_int_by_float():
    assert transform.transform(14, {}, {"value": 3.0}) == 2


def test_modulo_float_by_float():
    assert round(transform.transform(6.3, {}, {"value": 2}), 1) == 0.3


def test_modulo_float_by_float_string():
    assert round(transform.transform(6.3, {}, {"value": "2"}), 1) == 0.3


def test_modulo_float_by_int():
    assert round(transform.transform(6.3, {}, {"value": 2}), 1) == 0.3


def test_null_config():
    assert transform.transform(15, {}, {"value": None}) == 15


def test_no_config():
    assert transform.transform(15, {}, {}) == 15
