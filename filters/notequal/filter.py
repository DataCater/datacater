def filter(value, row: dict, config: dict) -> bool:
    if isinstance(value, bool):
        bool_value = config.get("value") in [
            "true",
            "True",
            "TRUE",
            "t",
            "y",
            "yes",
            "1",
            1,
        ]
        return value != bool_value
    elif isinstance(value, int):
        return value != int(config.get("value"))
    elif isinstance(value, float):
        return value != float(config.get("value"))
    else:
        return value != config.get("value")
