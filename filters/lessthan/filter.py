def filter(value, row: dict, config: dict) -> bool:
    if config.get("value", None) is None:
        return True
    elif isinstance(value, int):
        return value < int(config["value"])
    elif isinstance(value, float):
        return value < float(config["value"])
    else:
        return value < config["value"]
