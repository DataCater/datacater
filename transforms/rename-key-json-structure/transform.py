def transform(value, row, config):
    import json

    def rename_hook(obj):
        for key in list(obj):
            new_key = key.replace(config["oldKeyName"], config["newKeyName"])
            if new_key != key:
                obj[new_key] = obj[key]
                del obj[key]
        return obj

    return json.dumps(json.loads(value, object_hook = rename_hook))
