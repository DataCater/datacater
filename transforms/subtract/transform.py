def transform(value, row: dict, config: dict):
    if config.get("value") in [None, ""]:
        return value
    elif isinstance(value, float):
        return value - float(config["value"])
    elif isinstance(value, int):
        return value - int(config["value"])
