def transform(value, row: dict, config: dict):
    new_type = config.get("toDataType", "string")
    if new_type == "int" or new_type == "long":
        return int(value)
    elif new_type == "float" or new_type == "double":
        return float(value)
    elif new_type == "boolean":
        return value is not None and value in [
            "true",
            "True",
            "TRUE",
            "t",
            "y",
            "yes",
            "1",
            1,
        ]
    else:
        return str(value)
