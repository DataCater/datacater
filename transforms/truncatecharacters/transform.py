def transform(value: str, row: dict, config: dict) -> str:
    if config.get("characters") in [None, ""]:
        return value

    characters = int(config["characters"])

    assert characters >= 0

    return value[:characters]
