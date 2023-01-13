def transform(value, row: dict, config: dict):
    if config.get("value") is None:
        return value
    return config["value"]
