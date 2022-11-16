import multiplyfield.transform as transform
import pytest


def test_multiply_int_by_int():
    assert transform.transform(5, {"number": 3}, {"fieldName": "number"}) == 15


def test_multiply_int_by_float():
    assert transform.transform(5, {"number": 3.0}, {"fieldName": "number"}) == 15


def test_multiply_float_by_float():
    assert (
        round(transform.transform(6.3, {"number": 2.5}, {"fieldName": "number"}), 1)
        == 15.8
    )


def test_multiply_float_by_int():
    assert (
        round(transform.transform(6.3, {"number": 2}, {"fieldName": "number"}), 1)
        == 12.6
    )


def test_no_field():
    with pytest.raises(KeyError):
        transform.transform(15, {}, {"fieldName": "number"})


def test_no_config():
    with pytest.raises(KeyError):
        transform.transform(15, {}, {})
