def filter(value, row: dict, config: dict) -> bool:
    import re

    return config.get("value") is None or bool(re.match(config["value"], value))
