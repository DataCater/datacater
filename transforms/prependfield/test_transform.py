import prependfield.transform as transform
import pytest


def test_prepend_string():
    assert (
        transform.transform(
            "data streaming", {"prefix": "Developer-friendly "}, {"fieldName": "prefix"}
        )
        == "Developer-friendly data streaming"
    )


def test_prepend_int():
    with pytest.raises(TypeError):
        transform.transform("data streaming", {"prefix": 15}, {"fieldName": "prefix"})


def test_prepend_empty_string():
    assert (
        transform.transform("data streaming", {"prefix": ""}, {"fieldName": "prefix"})
        == "data streaming"
    )


def test_prepend_empty_field():
    with pytest.raises(KeyError):
        transform.transform("data streaming", {}, {"fieldName": "prefix"})


def test_null_config():
    assert (
        transform.transform("data streaming", {"prefix": "Python"}, {"fieldName": None})
        == "data streaming"
    )


def test_no_config():
    assert (
        transform.transform("data streaming", {"prefix": "Python"}, {})
        == "data streaming"
    )
