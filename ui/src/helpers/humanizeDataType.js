export function humanizeDataType(dataType) {
  if (dataType.includes("array_")) {
    const capitalizedDataType = dataType
      .replace("array_", "")
      .split("-")
      .map(
        (subStr) =>
          subStr.charAt(0).toUpperCase() + subStr.substr(1).toLowerCase()
      )
      .join("-");
    return "List[" + capitalizedDataType + "]";
  } else {
    const firstPart = dataType.split("-")[0];
    return (
      firstPart.charAt(0).toUpperCase() + firstPart.substr(1).toLowerCase()
    );
  }
}
