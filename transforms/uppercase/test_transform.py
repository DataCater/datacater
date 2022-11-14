import uppercase.transform as transform
import pytest


def test_uppercase_string():
    assert transform.transform("data streaming", {}, {}) == "DATA STREAMING"


def test_uppercase_string_identity():
    assert transform.transform("DATA STREAMING", {}, {}) == "DATA STREAMING"


def test_uppercase_empty_string():
    assert transform.transform("", {}, {}) == ""


def test_uppercase_empty_value():
    with pytest.raises(AttributeError):
        assert transform.transform(None, {}, {}) == ""
