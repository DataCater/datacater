import ceil.transform as transform
import pytest


def test_ceil_int():
    assert transform.transform(15, {}, {}) == 15


def test_ceil_float():
    assert transform.transform(15.13, {}, {}) == 16
    assert transform.transform(15.83, {}, {}) == 16


def test_ceil_none():
    with pytest.raises(TypeError):
        transform.transform(None, {}, {})
