import regexp.filter as filter
import pytest


def test_string_regexp_matches():
    assert (
        filter.filter(
            "DataCater is the developer-friendly ETL platform for transforming data in motion.",
            {},
            {"value": ".*ETL.*"},
        )
        is True
    )


def test_string_regexp_does_not_match():
    assert (
        filter.filter(
            "DataCater is the developer-friendly ETL platform for transforming data in motion.",
            {},
            {"value": ".*ELT.*"},
        )
        is False
    )


def test_string_regexp_null_config():
    assert filter.filter(
        "DataCater is the developer-friendly ETL platform for transforming data in motion.",
        {},
        {"value": None},
    )


def test_string_regexp_no_config():
    assert filter.filter(
        "DataCater is the developer-friendly ETL platform for transforming data in motion.",
        {},
        {},
    )
