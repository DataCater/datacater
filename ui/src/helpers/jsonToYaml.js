const yaml = require("js-yaml");

export function jsonToYaml(json) {
  return yaml.dump(json, { lineWidth: -1 });
}
