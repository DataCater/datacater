import replaceconstant.transform as transform
import pytest


def test_returns_value_string():
    assert transform.transform(None, {}, {"value": "DataCater"}) == "DataCater"


def test_returns_value_int():
    assert transform.transform(None, {}, {"value": 42}) == 42


def test_returns_value_none():
    assert transform.transform(None, {}, {"value": None}) == None


def test_no_config():
    assert transform.transform(None, {}, {}) == None
