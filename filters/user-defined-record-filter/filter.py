def filter(record, config):
    default_filter = "def filter(record):\n  return True"

    scope = {}
    exec(config.get("code", default_filter), scope)

    return scope["filter"](record)
