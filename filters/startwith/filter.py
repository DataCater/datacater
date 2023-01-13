def filter(value, row: dict, config: dict) -> bool:
    return config.get("value") is None or value.startswith(config["value"])
