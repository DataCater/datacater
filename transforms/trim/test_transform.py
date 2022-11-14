import trim.transform as transform
import pytest


def test_trim_whitespaces():
    assert (
        transform.transform(
            " DataCater is the developer-friendly ETL platform. ", {}, {}
        )
        == "DataCater is the developer-friendly ETL platform."
    )


def test_trim_tabs():
    assert (
        transform.transform(
            " DataCater is the developer-friendly ETL platform.\t\t", {}, {}
        )
        == "DataCater is the developer-friendly ETL platform."
    )


def test_trim_empty_string():
    assert transform.transform("", {}, {}) == ""
    assert transform.transform("\t \t", {}, {}) == ""
