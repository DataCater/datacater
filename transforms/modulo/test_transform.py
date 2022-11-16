import modulo.transform as transform
import pytest


def test_modulo_int_by_int():
    assert transform.transform(14, {}, {"value": 3}) == 2


def test_modulo_int_by_float():
    assert transform.transform(14, {}, {"value": 3.0}) == 2


def test_modulo_float_by_float():
    assert round(transform.transform(6.3, {}, {"value": 2}), 1) == 0.3


def test_modulo_float_by_int():
    assert round(transform.transform(6.3, {}, {"value": 2}), 1) == 0.3


def test_no_config():
    with pytest.raises(KeyError):
        transform.transform(15, {}, {})
