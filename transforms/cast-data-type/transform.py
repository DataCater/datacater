def transform(value, row, config):
    new_type = config.get("toDataType", "string")
    if new_type == "int" or new_type == "long":
        return int(value)
    elif new_type == "float" or new_type == "double":
        return float(value)
    elif new_type == "boolean":
        return bool(value)
    else:
        return str(value)
