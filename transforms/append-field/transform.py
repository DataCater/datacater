def transform(value: str, row: dict, config: dict) -> str:
    return value.join(row[config["fieldName"]])
