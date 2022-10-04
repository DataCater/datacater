export function parseQueryString(queryString) {
  if (queryString !== undefined && queryString.length > 0) {
    let params = {};

    // ignore leading question mark
    let cleanQueryString = queryString;
    if (queryString[0] === "?") {
      cleanQueryString = queryString.substring(1);
    }

    cleanQueryString.split("&").forEach((param) => {
      const parts = param.split("=");

      params[parts[0]] = parts[1];
    });

    return params;
  } else {
    return {};
  }
}
