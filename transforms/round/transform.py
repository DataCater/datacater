def transform(value, row: dict, config: dict):
    return round(value, int(config["precision"]))
