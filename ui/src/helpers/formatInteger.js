export function formatInteger(number) {
  const castedNumber = parseInt(number);

  // return parameter if it is not a valid integer
  if (isNaN(castedNumber)) {
    return number;
  }

  // add thousand delimiters to number
  return castedNumber.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ",");
}
