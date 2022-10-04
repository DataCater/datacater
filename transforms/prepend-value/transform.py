def transform(value, row, config):
    return config.get("value", "").join(value)
