def transform(value: str, row: dict, config: dict) -> str:
    characters = int(config["characters"])
    return value[:characters]
