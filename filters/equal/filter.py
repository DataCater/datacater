def filter(value, row: dict, config: dict) -> bool:
    return value == config.get("value", None)
