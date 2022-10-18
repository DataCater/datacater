def transform(record, config):
    default_transform = "def transform(record):\n  return record"

    scope = {}
    exec(config.get("code", default_transform), scope)

    return scope["transform"](record)
