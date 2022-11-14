import truncatewords.transform as transform
import pytest


def test_truncate_words():
    assert (
        transform.transform(
            "DataCater is the developer-friendly ETL platform.", {}, {"words": 4}
        )
        == "DataCater is the developer-friendly"
    )


def test_truncate_words_token():
    assert (
        transform.transform(
            "DataCater is the developer-friendly ETL platform.",
            {},
            {"words": 1, "token": "-"},
        )
        == "DataCater is the developer"
    )


def test_truncate_words_passed_as_string():
    assert (
        transform.transform(
            "DataCater is the developer-friendly ETL platform.", {}, {"words": "2"}
        )
        == "DataCater is"
    )


def test_truncate_words_no_change():
    assert (
        transform.transform(
            "DataCater is the developer-friendly ETL platform.", {}, {"words": 1000}
        )
        == "DataCater is the developer-friendly ETL platform."
    )


def test_truncate_words_empty_string():
    assert transform.transform("", {}, {"words": 1000}) == ""


def test_no_config():
    with pytest.raises(KeyError):
        transform.transform("", {}, {})
