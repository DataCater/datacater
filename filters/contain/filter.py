def filter(value, row: dict, config: dict) -> bool:
    return config.get("value", None) in value
