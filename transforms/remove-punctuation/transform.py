def transform(value, row, config):
    import re
    punctuation_chars = r'/(\\t|\\n|!|"|\'|#|\$|%|&|\(|\)|\*|\+|,|-|\.|\/|:|;|<|=|>|\?|@|\[|\\|\]|\^|_|`\{|\||\}|~)/g'
    return re.sub(punctuation_chars, '', value)
