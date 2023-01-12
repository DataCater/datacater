def transform(value, row: dict, config: dict):
    if config.get("precision") in [None, ""]:
        return value
    return round(value, int(config["precision"]))
