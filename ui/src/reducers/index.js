import { combineReducers } from "redux";
import filters from "./filters";
import pipelines from "./pipelines";
import pipelineDesigner from "./pipeline_designer";
import streams from "./streams";
import transforms from "./transforms";
import userSessions from "./user_sessions";

export default combineReducers({
  filters,
  pipelines,
  pipelineDesigner,
  streams,
  transforms,
  userSessions,
});
