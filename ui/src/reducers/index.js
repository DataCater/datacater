import { combineReducers } from "redux";
import connectors from "./connectors";
import deployments from "./deployments";
import filters from "./filters";
import pipelines from "./pipelines";
import streams from "./streams";
import configs from "./configs";
import info from "./info";
import transforms from "./transforms";
import userSessions from "./user_sessions";

export default combineReducers({
  connectors,
  deployments,
  configs,
  filters,
  pipelines,
  streams,
  transforms,
  info,
  userSessions,
});
