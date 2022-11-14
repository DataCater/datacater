import subtractfield.transform as transform
import pytest


def test_subtract_int_from_int():
    assert (
        transform.transform(15, {"magicNumber": 3}, {"fieldName": "magicNumber"}) == 12
    )


def test_subtract_int_from_float():
    assert (
        transform.transform(15.0, {"magicNumber": 3}, {"fieldName": "magicNumber"})
        == 12
    )


def test_subtract_float_from_int():
    assert (
        transform.transform(15, {"magicNumber": 3.0}, {"fieldName": "magicNumber"})
        == 12
    )


def test_subtract_string_from_int():
    with pytest.raises(TypeError):
        transform.transform(15, {"magicNumber": "2"}, {"fieldName": "magicNumber"})


def test_no_config():
    with pytest.raises(KeyError):
        transform.transform(15, {"magicNumber": "2"}, {})
