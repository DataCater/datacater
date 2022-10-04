const axios = require("axios");

import { getApiPathPrefix } from "./getApiPathPrefix";

export function callApi(uri, config = {}) {
  const apiPrefix = getApiPathPrefix();

  config.headers = Object.assign({}, config.headers, {
    Authorization: `Bearer ${localStorage.getItem("userToken")}`,
  });

  return axios.request(
    Object.assign({}, config, { url: apiPrefix.concat(uri) })
  );
}
