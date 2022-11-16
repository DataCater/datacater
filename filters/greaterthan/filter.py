def filter(value, row: dict, config: dict) -> bool:
    return value > config["value"]
