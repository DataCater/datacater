def transform(value: str, row: dict, config: dict) -> str:
    return row[config["fieldName"]].join(value)
