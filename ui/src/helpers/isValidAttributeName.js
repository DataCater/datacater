// returns true, if the given attribute name is allowed
// returns false, if not
export function isValidAttributeName(attributeName) {
  const validAttributeName = /^([a-zA-Z0-9_-]+|[ß äöüÄÖÜ])+$/g;

  const isValid =
    // check whether attribute name is defined
    attributeName !== undefined &&
    // check whether attribute name is of type string
    typeof attributeName === "string" &&
    // check whether attribute name matches reg exp
    attributeName.match(validAttributeName) != null;

  return isValid;
}
