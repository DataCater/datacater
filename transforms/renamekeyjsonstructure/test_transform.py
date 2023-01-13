import renamekeyjsonstructure.transform as transform
import json
import pytest


def test_rename_key():
    assert json.loads(
        transform.transform(
            '{"name": "DataCater GmbH", "url": "https://datacater.io"}',
            {},
            {"oldKeyName": "name", "newKeyName": "companyName"},
        )
    ) == {"companyName": "DataCater GmbH", "url": "https://datacater.io"}


def test_rename_key_in_nested_structure():
    assert json.loads(
        transform.transform(
            '{"info": { "name": "DataCater GmbH" }, "url": "https://datacater.io"}',
            {},
            {"oldKeyName": "name", "newKeyName": "companyName"},
        )
    ) == {"info": {"companyName": "DataCater GmbH"}, "url": "https://datacater.io"}


def test_no_config():
    json_obj = '{"info": { "name": "DataCater GmbH" }, "url": "https://datacater.io"}'
    assert (
        transform.transform(
            json_obj,
            {},
            {},
        )
        == json_obj
    )
