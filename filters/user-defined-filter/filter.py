def filter(value, row: dict, config: dict) -> bool:
    default_filter = "def filter(value, row):\n  return True"

    scope = {}
    exec(config.get("code", default_filter), scope)

    return scope["filter"](value, row)
