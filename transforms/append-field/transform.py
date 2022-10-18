def transform(value, row, config):
    return value.join(row[config["fieldName"]])
