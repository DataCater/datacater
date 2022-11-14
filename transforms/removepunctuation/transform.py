def transform(value: str, row: dict, config: dict) -> str:
    import re

    punctuation_chars = r'(\\t|\\n|!|"|\'|#|\$|%|&|\(|\)|\*|\+|,|-|\.|\/|:|;|<|=|>|\?|@|\[|\\|\]|\^|_|`\{|\||\}|~)'
    return re.sub(punctuation_chars, "", value)
