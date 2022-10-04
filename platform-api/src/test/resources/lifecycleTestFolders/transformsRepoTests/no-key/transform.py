def transform(value, row, config):
    return value.join(config.get("value", ""))
