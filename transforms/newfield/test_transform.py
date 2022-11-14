import newfield.transform as transform
import pytest


def test_returns_none_by_default():
    assert transform.transform(None, {}, {}) == None


def test_returns_default_value():
    assert transform.transform(None, {}, {"defaultValue": 42}) == 42
