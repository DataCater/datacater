def transform(value, row, config):
    if config.get("fieldName") is None:
        return value
    return row[config["fieldName"]]
