import hash.transform as transform
import pytest
import hashlib


def test_sha512():
    assert transform.transform("DataCater", {}, {"algorithm": "sha512"}) == str(
        hashlib.sha512("DataCater".encode("utf-8")).hexdigest()
    )


def test_sha256():
    assert transform.transform("DataCater", {}, {"algorithm": "sha256"}) == str(
        hashlib.sha256("DataCater".encode("utf-8")).hexdigest()
    )


def test_sha1():
    assert transform.transform("DataCater", {}, {"algorithm": "sha1"}) == str(
        hashlib.sha1("DataCater".encode("utf-8")).hexdigest()
    )


def test_default():
    assert transform.transform("DataCater", {}, {}) == str(
        hashlib.sha1("DataCater".encode("utf-8")).hexdigest()
    )


def test_empty_string():
    assert transform.transform("", {}, {}) == str(
        hashlib.sha1("".encode("utf-8")).hexdigest()
    )


def test_empty_value():
    with pytest.raises(AttributeError):
        transform.transform(None, {}, {})
