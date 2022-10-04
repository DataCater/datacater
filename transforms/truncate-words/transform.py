def transform(value, row, config):
    token = config.get("token", " ")
    words = int(config["words"])

    return token.join(value.split(token)[:words])
