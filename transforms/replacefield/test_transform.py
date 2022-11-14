import replacefield.transform as transform
import pytest


def test_returns_field_string():
    assert (
        transform.transform(None, {"name": "DataCater"}, {"fieldName": "name"})
        == "DataCater"
    )


def test_returns_value_int():
    assert (
        transform.transform(None, {"magicNumber": 42}, {"fieldName": "magicNumber"})
        == 42
    )


def test_returns_value_field_does_not_exist():
    with pytest.raises(KeyError):
        assert transform.transform(None, {}, {"fieldName": "name"}) == None


def test_no_config():
    with pytest.raises(KeyError):
        assert transform.transform(None, {}, {})
