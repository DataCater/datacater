import startwith.filter as filter
import pytest

def test_string_starts_with_substring():
    assert filter.filter(
        "DataCater is the developer-friendly ETL platform for transforming data in motion.",
        {},
        { "value": "DataCater is the developer-friendly ETL platform" })

def test_string_does_not_start_with_substring():
    assert filter.filter(
        "DataCater is the developer-friendly ETL platform for transforming data in motion.",
        {},
        { "value": "DataCater does not like developers" }
    ) == False

def test_no_config():
    with pytest.raises(KeyError):
        filter.filter(
            "DataCater is the developer-friendly ETL platform for transforming data in motion.",
            {},
            {}
        )
