export function formatDate(dateString) {
  const dateObj = new Date(dateString);

  // check whether date is valid
  if (isNaN(dateObj.getTime())) {
    return "";
  }

  return dateObj.toLocaleString("en-US");
}
