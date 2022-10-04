def transform(value, row, config):
    return row[config["attributeName"]].join(value)
