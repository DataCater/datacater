def transform(value: str, row: dict, config: dict) -> str:
    characters = int(config["characters"])

    assert characters >= 0

    return value[:characters]
