import lowercase.transform as transform
import pytest


def test_lowercase_string():
    assert transform.transform("Data Streaming", {}, {}) == "data streaming"


def test_lowercase_string_identity():
    assert transform.transform("data streaming", {}, {}) == "data streaming"


def test_lowercase_empty_string():
    assert transform.transform("", {}, {}) == ""


def test_lowercase_empty_value():
    with pytest.raises(AttributeError):
        assert transform.transform(None, {}, {}) == ""
