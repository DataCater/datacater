def transform(value, row: dict, config: dict):
    return value % row[config["fieldName"]]
