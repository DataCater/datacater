def filter(value, row: dict, config: dict) -> bool:
    import re
    return bool(re.match(config["value"], value))
