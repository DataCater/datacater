import { combineReducers } from "redux";
import deployments from "./deployments";
import filters from "./filters";
import pipelines from "./pipelines";
import streams from "./streams";
import configs from "./configs";
import info from "./info";
import projects from "./projects";
import transforms from "./transforms";
import userSessions from "./user_sessions";

export default combineReducers({
  deployments,
  configs,
  filters,
  pipelines,
  streams,
  transforms,
  info,
  projects,
  userSessions,
});
