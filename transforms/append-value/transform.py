def transform(value: str, row: dict, config: dict) -> str:
    return value.join(config.get("value", ""))
