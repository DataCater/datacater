def transform(value: str, row: dict, config: dict) -> str:
    if config.get("value") is None:
        return value
    # See performance of string concatenation in Python:
    # https://stackoverflow.com/questions/12169839/which-is-the-preferred-way-to-concatenate-a-string-in-python/12171382#12171382
    return value + config["value"]
