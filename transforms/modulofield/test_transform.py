import modulofield.transform as transform
import pytest


def test_modulo_int_by_int():
    assert transform.transform(14, {"number": 3}, {"fieldName": "number"}) == 2


def test_modulo_int_by_float():
    assert transform.transform(14, {"number": 3.0}, {"fieldName": "number"}) == 2


def test_modulo_float_by_float():
    assert (
        round(transform.transform(6.3, {"number": 2.0}, {"fieldName": "number"}), 1)
        == 0.3
    )


def test_modulo_float_by_int():
    assert (
        round(transform.transform(6.3, {"number": 2}, {"fieldName": "number"}), 1)
        == 0.3
    )


def test_no_field():
    with pytest.raises(KeyError):
        transform.transform(15, {}, {"fieldName": "number"})


def test_null_config():
    assert transform.transform(15, {}, {"fieldName": None}) == 15


def test_no_config():
    assert transform.transform(15, {}, {}) == 15
