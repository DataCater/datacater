import truncatecharacters.transform as transform
import pytest


def test_truncate_characters():
    assert (
        transform.transform(
            "DataCater is the developer-friendly ETL platform.", {}, {"characters": 9}
        )
        == "DataCater"
    )


def test_truncate_characters_passed_as_string():
    assert (
        transform.transform(
            "DataCater is the developer-friendly ETL platform.", {}, {"characters": "9"}
        )
        == "DataCater"
    )


def test_truncate_characters_no_change():
    assert (
        transform.transform(
            "DataCater is the developer-friendly ETL platform.",
            {},
            {"characters": 1000},
        )
        == "DataCater is the developer-friendly ETL platform."
    )


def test_truncate_negative_number_of_characters():
    with pytest.raises(AssertionError):
        transform.transform(
            "DataCater is the developer-friendly ETL platform.",
            {},
            {"characters": -1},
        )


def test_truncate_characters_empty_string():
    assert transform.transform("", {}, {"characters": 1000}) == ""


def test_null_config():
    assert transform.transform("", {}, {"characters": None}) == ""


def test_no_config():
    assert transform.transform("", {}, {}) == ""
