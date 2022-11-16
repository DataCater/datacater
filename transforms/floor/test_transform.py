import floor.transform as transform
import pytest


def test_floor_int():
    assert transform.transform(15, {}, {}) == 15


def test_floor_float():
    assert transform.transform(15.13, {}, {}) == 15
    assert transform.transform(15.83, {}, {}) == 15


def test_floor_none():
    with pytest.raises(TypeError):
        transform.transform(None, {}, {})
