import prependvalue.transform as transform
import pytest


def test_prepend_string():
    assert (
        transform.transform("data streaming", {}, {"value": "Developer-friendly "})
        == "Developer-friendly data streaming"
    )


def test_prepend_int():
    with pytest.raises(TypeError):
        transform.transform("data streaming", {}, {"value": 15})


def test_prepend_empty_string():
    assert transform.transform("data streaming", {}, {"value": ""}) == "data streaming"


def test_null_config():
    assert (
        transform.transform("data streaming", {}, {"value": None}) == "data streaming"
    )


def test_no_config():
    assert transform.transform("data streaming", {}, {}) == "data streaming"
