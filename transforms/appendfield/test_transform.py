import appendfield.transform as transform
import pytest


def test_append_string():
    assert (
        transform.transform(
            "Data streaming with ",
            {"programmingLanguage": "Python"},
            {"fieldName": "programmingLanguage"},
        )
        == "Data streaming with Python"
    )


def test_append_int():
    with pytest.raises(TypeError):
        transform.transform(
            "Data streaming with ",
            {"programmingLanguage": 15},
            {"fieldName": "programmingLanguage"},
        )


def test_append_empty_string():
    assert (
        transform.transform(
            "Data streaming with ",
            {"programmingLanguage": ""},
            {"fieldName": "programmingLanguage"},
        )
        == "Data streaming with "
    )


def test_append_empty_field():
    with pytest.raises(KeyError):
        transform.transform(
            "Data streaming with ", {}, {"fieldName": "programmingLanguage"}
        )


def test_null_config():
    assert (
        transform.transform(
            "Data streaming with ",
            {"programmingLanguage": "Python"},
            {"fieldName": None},
        )
        == "Data streaming with "
    )


def test_no_config():
    assert (
        transform.transform(
            "Data streaming with ", {"programmingLanguage": "Python"}, {}
        )
        == "Data streaming with "
    )
