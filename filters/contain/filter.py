def filter(value, row: dict, config: dict) -> bool:
    return config.get("value") is None or config["value"] in value
