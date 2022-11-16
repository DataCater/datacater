import removepunctuation.transform as transform
import pytest


def test_remove_punctuation_characters():
    assert (
        transform.transform("Is DataCater streaming data in motion? Yes!", {}, {})
        == "Is DataCater streaming data in motion Yes"
    )
