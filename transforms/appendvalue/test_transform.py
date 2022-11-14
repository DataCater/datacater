import appendvalue.transform as transform
import pytest


def test_append_string():
    assert (
        transform.transform("Data streaming with ", {}, {"value": "Python"})
        == "Data streaming with Python"
    )


def test_append_int():
    with pytest.raises(TypeError):
        transform.transform("Data streaming with ", {}, {"value": 15})


def test_append_empty_string():
    assert (
        transform.transform("Data streaming with ", {}, {"value": ""})
        == "Data streaming with "
    )


def test_no_config():
    with pytest.raises(KeyError):
        transform.transform("Data streaming with ", {}, {})
