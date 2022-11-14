import castdatatype.transform as transform
import pytest


def test_string_to_int():
    assert transform.transform("15", {}, {"toDataType": "int"}) == 15


def test_string_to_float():
    assert transform.transform("15.13", {}, {"toDataType": "float"}) == 15.13


def test_string_to_boolean_true():
    assert transform.transform("True", {}, {"toDataType": "boolean"}) is True
    assert transform.transform("true", {}, {"toDataType": "boolean"}) is True


def test_string_to_boolean_false():
    assert transform.transform("False", {}, {"toDataType": "boolean"}) is False
    assert transform.transform("false", {}, {"toDataType": "boolean"}) is False


def number_to_boolean():
    assert transform.transform(1, {}, {"toDataType": "boolean"}) is True
    assert transform.transform(1.0, {}, {"toDataType": "boolean"}) is True
    assert transform.transform(0, {}, {"toDataType": "boolean"}) is False


def test_int_to_string():
    assert transform.transform(15, {}, {"toDataType": "string"}) == "15"


def test_float_to_string():
    assert transform.transform(15.13, {}, {"toDataType": "string"}) == "15.13"


def test_boolean_true_to_string():
    assert transform.transform(True, {}, {"toDataType": "string"}) == "True"


def test_boolean_false_to_string():
    assert transform.transform(False, {}, {"toDataType": "string"}) == "False"


def test_cast_defaults_to_string():
    assert transform.transform(15, {}, {}) == "15"
