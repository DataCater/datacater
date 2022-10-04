def filter(value, row, config):
    import re
    return bool(re.match(config["value"], value))
