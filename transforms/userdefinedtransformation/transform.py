def transform(value, row: dict, config: dict):
    default_transform = "def transform(value, row):\n  return value"

    scope = {}
    exec(config.get("code", default_transform), scope)

    return scope["transform"](value, row)
