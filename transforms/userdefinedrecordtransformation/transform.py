def transform(record: dict, config: dict) -> dict:
    default_transform = "def transform(record):\n  return record"

    scope = {}
    exec(config.get("code", default_transform), scope)

    return scope["transform"](record)
