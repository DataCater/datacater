def transform(value, row: dict, config: dict):
    if config.get("fieldName") in [None, ""]:
        return value
    return value + row[config["fieldName"]]
