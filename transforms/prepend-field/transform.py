def transform(value, row, config):
    return row[config["fieldName"]].join(value)
