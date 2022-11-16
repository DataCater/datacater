import tokenizer.transform as transform
import pytest


def test_tokenize_without_token():
    assert transform.transform("developer-friendly ETL", {}, {}) == [
        "developer-friendly",
        "ETL",
    ]


def test_tokenize_with_token():
    assert transform.transform("developer-friendly ETL", {}, {"token": "-"}) == [
        "developer",
        "friendly ETL",
    ]


def test_tokenize_empty_string():
    assert transform.transform("", {}, {}) == [""]
