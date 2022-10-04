def transform(value, row, config):
    characters = int(config["characters"])
    return value[:characters]
