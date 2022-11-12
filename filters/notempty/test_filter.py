import notempty.filter as filter
import pytest


def test_value_is_none():
    assert filter.filter(None, {}, {}) is False


def test_value_is_string():
    assert (
        filter.filter(
            "DataCater is the developer-friendly ETL platform for transforming data in motion.",
            {},
            {},
        )
        is True
    )


def test_value_is_empty_string():
    assert filter.filter("", {}, {}) is True
