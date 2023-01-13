def transform(value: str, row: dict, config: dict) -> str:
    import json

    if config.get("oldKeyName") in [None, ""] or config.get("newKeyName") in [None, ""]:
        return value

    def rename_hook(obj):
        for key in list(obj):
            new_key = key.replace(config["oldKeyName"], config["newKeyName"])
            if new_key != key:
                obj[new_key] = obj[key]
                del obj[key]
        return obj

    return json.dumps(json.loads(value, object_hook=rename_hook))
