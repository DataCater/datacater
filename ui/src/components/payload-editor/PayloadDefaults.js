
const STREAM = {
  "kind": "KAFKA",
  "spec": {
    "kafka": {},
    "topic": {}
  }
};

const PIPELINE = {
  "kind": "pipeline",
  "spec": {}
};
const DEPLOYMENT = {
  "kind": "deployment",
  "spec": {}
};

const CONFIG = {
  "kind": "config",
  "spec": {}
};

const staticMap = new Map([
  ["streams", STREAM],
  ["pipelines", PIPELINE],
  ["deployments", DEPLOYMENT],
  ["configs", CONFIG]
]);

Object.freeze(staticMap);

/**
 * This constant function returns a default spec according to the apiPath from the Header component.
 *
 * @param apiPath
 * @constructor
 */
const getByString = (apiPath) => {
  // remove leading and potentially trailing slash
  console.debug(`Received apiPath := ${apiPath}`);
  const cleansedPath = apiPath.replaceAll("/", "").toLowerCase();
  console.debug(`Will match against cleansedPath := ${cleansedPath}`);

  staticMap.set("hello", {});
  return staticMap.get(cleansedPath);
}

Object.freeze(getByString);

export { STREAM, PIPELINE, DEPLOYMENT, CONFIG, getByString };
