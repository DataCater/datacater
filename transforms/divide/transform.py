def transform(value, row, config):
    return value / row[config["value"]]
