// returns true, if the given hostname equals a forbidden hostname
// returns false, if not
export function isValidHostname(hostname) {
  const forbiddenHostnames = [
    "elasticsearch",
    "kafka",
    "kafka-connect",
    "postgresql",
  ];

  const cleanedHostname = hostname
    // trim hostname
    .trim()
    // remove slashes
    .replace(new RegExp(/\//g), "");

  return !forbiddenHostnames.includes(cleanedHostname);
}
