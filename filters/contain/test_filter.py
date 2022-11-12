import contain.filter as filter
import pytest

def test_string_contains_substring():
    assert filter.filter(
        "DataCater is the developer-friendly ETL platform for transforming data in motion.",
        {},
        { "value": "ETL" })

def test_string_does_not_contain_substring():
    assert filter.filter(
        "DataCater is the developer-friendly ETL platform for transforming data in motion.",
        {},
        { "value": "Reverse ETL" }
    ) is False

def test_no_config():
    with pytest.raises(KeyError):
        filter.filter(
            "DataCater is the developer-friendly ETL platform for transforming data in motion.",
            {},
            {}
        )
