import round.transform as transform
import pytest


def test_round_int():
    assert transform.transform(8, {}, {"precision": 1}) == 8


def test_round_float_downwards():
    assert transform.transform(8.14, {}, {"precision": 1}) == 8.1


def test_round_float_upwards():
    assert transform.transform(8.16, {}, {"precision": 1}) == 8.2


def test_round_precision():
    assert transform.transform(8.13472, {}, {"precision": 3}) == 8.135


def test_round_precision_passed_as_string():
    assert transform.transform(8.13472, {}, {"precision": "3"}) == 8.135


def test_no_config():
    with pytest.raises(KeyError):
        assert transform.transform(8.14, {}, {})
