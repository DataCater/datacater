def transform(value: str, row: dict, config: dict) -> list[str]:
    token = config.get("token", " ")
    return value.split(token)
