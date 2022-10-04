def transform(value, row, config):
    token = config.get("token", " ")
    return value.split(token)
