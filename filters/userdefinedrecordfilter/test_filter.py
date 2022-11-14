import userdefinedrecordfilter.filter as filter
import pytest


def test_udf_returns_true():
    assert (
        filter.filter(
            {"value": {"foo": 42}},
            {"code": 'def filter(record):\n  return record["value"]["foo"] == 42'},
        )
        is True
    )


def test_udf_returns_false():
    assert (
        filter.filter(
            {"value": {"foo": "42"}},
            {"code": 'def filter(record):\n  return record["value"]["foo"] == 42'},
        )
        is False
    )


def test_udf_with_wrong_function_name_throws_exception():
    with pytest.raises(KeyError):
        filter.filter(
            {"value": {"foo": "42"}},
            {"code": 'def filterfoo(record):\n  return record["value"]["foo"] == 42'},
        )


def test_udf_with_wrong_parameters_throws_exception():
    with pytest.raises(TypeError):
        filter.filter(
            {"value": {"foo": "42"}},
            {
                "code": 'def filter(record, config):\n  return record["value"]["foo"] == 42'
            },
        )


def test_no_config_returns_true():
    assert filter.filter({}, {}) is True
