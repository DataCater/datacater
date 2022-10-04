const rfdc = require("rfdc")();

export function deepCopy(object) {
  return rfdc(object);
}
