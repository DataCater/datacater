def filter(value, row, config):
    return value.endswith(config.get("value", None))
