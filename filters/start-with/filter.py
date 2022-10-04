def filter(value, row, config):
    return value.startswith(config.get("value", None))
