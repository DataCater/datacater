def transform(value: str, row: dict, config: dict) -> str:
    import hashlib

    algorithm = config.get("algorithm", "sha1")
    byte_value = value.encode("utf-8")

    if algorithm == "sha512":
        return hashlib.sha512(byte_value).hexdigest()
    elif algorithm == "sha256":
        return hashlib.sha256(byte_value).hexdigest()
    else:  # default: sha1
        return hashlib.sha1(byte_value).hexdigest()
