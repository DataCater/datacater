import capitalize.transform as transform
import pytest


def test_string():
    assert transform.transform("data streaming", {}, {}) == "Data streaming"


def test_empty_string():
    assert transform.transform("", {}, {}) == ""


def test_none_value():
    with pytest.raises(AttributeError):
        transform.transform(None, {}, {})
