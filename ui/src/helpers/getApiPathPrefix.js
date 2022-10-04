export function getApiPathPrefix(includeHostname = false) {
  const hostname = window.location.protocol + "//" + window.location.host;
  const pathPrefix = "/api/alpha";

  if (includeHostname) {
    return hostname + pathPrefix;
  } else {
    return pathPrefix;
  }
}
