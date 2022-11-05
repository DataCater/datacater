import { combineReducers } from "redux";
import deployments from "./deployments";
import filters from "./filters";
import pipelines from "./pipelines";
import streams from "./streams";
import transforms from "./transforms";
import userSessions from "./user_sessions";

export default combineReducers({
  deployments,
  filters,
  pipelines,
  streams,
  transforms,
  userSessions,
});
