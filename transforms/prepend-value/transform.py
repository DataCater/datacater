def transform(value: str, row: dict, config: dict) -> str:
    return config.get("value", "").join(value)
