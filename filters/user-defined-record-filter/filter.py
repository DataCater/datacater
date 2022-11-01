def filter(record: dict, config: dict) -> bool:
    default_filter = "def filter(record):\n  return True"

    scope = {}
    exec(config.get("code", default_filter), scope)

    return scope["filter"](record)
