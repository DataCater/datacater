def filter(value, row: dict, config: dict) -> bool:
    return value.startswith(config["value"])
