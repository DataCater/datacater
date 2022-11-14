def transform(value: str, row: dict, config: dict) -> str:
    token = config.get("token", " ")
    words = int(config["words"])

    assert words >= 0

    return token.join(value.split(token)[:words])
