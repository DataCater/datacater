import endwith.filter as filter
import pytest


def test_string_ends_with_substring():
    assert filter.filter(
        "DataCater is the developer-friendly ETL platform for transforming data in motion.",
        {},
        {"value": "data in motion."},
    )


def test_string_does_not_end_with_substring():
    assert (
        filter.filter(
            "DataCater is the developer-friendly ETL platform for transforming data in motion.",
            {},
            {"value": "data at rest."},
        )
        == False
    )


def test_no_config():
    with pytest.raises(KeyError):
        filter.filter(
            "DataCater is the developer-friendly ETL platform for transforming data in motion.",
            {},
            {},
        )
