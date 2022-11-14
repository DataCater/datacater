import addfield.transform as transform
import pytest


def test_add_int_to_int():
    assert transform.transform(15, {"year": 2022}, {"fieldName": "year"}) == 2037


def test_add_int_to_float():
    assert transform.transform(15.0, {"year": 2022}, {"fieldName": "year"}) == 2037


def test_add_float_to_int():
    assert transform.transform(15, {"year": 2022.0}, {"fieldName": "year"}) == 2037


def test_add_string_to_int():
    with pytest.raises(TypeError):
        transform.transform(15, {"year": "2022"}, {"fieldName": "year"})


def test_no_config():
    with pytest.raises(KeyError):
        transform.transform(15, {"year": "2022"}, {})
