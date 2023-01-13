import add.transform as transform
import pytest


def test_add_int_to_int():
    assert transform.transform(15, {}, {"value": 2022}) == 2037


def test_add_int_to_float():
    assert transform.transform(15.0, {}, {"value": 2022.0}) == 2037


def test_add_float_to_int():
    assert transform.transform(15, {}, {"value": 2022}) == 2037


def test_add_string_to_int():
    assert transform.transform(15, {}, {"value": "2022"}) == 2037


def test_null_config():
    assert transform.transform(15, {}, {"value": None}) == 15


def test_no_config():
    assert transform.transform(15, {}, {}) == 15
