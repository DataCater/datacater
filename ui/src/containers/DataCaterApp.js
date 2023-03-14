import React, { Component } from "react";
import { BrowserRouter, Route, Redirect, Switch } from "react-router-dom";
import { ErrorBoundary } from "react-error-boundary";
import { PrivateRoute } from "../components/PrivateRoute";
import MainLayout from "../components/layout/MainLayout";
import Home from "./Home";
import Login from "./Login";
import ListDeployments from "./deployments/ListDeployments";
import NewDeployment from "./deployments/NewDeployment";
import ShowDeployment from "./deployments/ShowDeployment";
import EditDeployment from "./deployments/EditDeployment";
import ListConfigs from "./configs/ListConfigs";
import NewConfig from "./configs/NewConfig";
import ShowConfig from "./configs/ShowConfig";
import EditConfig from "./configs/EditConfig";
import DeploymentLogs from "./deployments/DeploymentLogs";
import ListPipelines from "./pipelines/ListPipelines";
import NewPipeline from "./pipelines/NewPipeline";
import ShowPipeline from "./pipelines/ShowPipeline";
import PipelineSettings from "./pipelines/PipelineSettings";
import EditPipeline from "./pipelines/EditPipeline";
import ListStreams from "./streams/ListStreams";
import ShowStream from "./streams/ShowStream";
import InspectStream from "./streams/InspectStream";
import NewStream from "./streams/NewStream";
import EditStream from "./streams/EditStream";
import NotFoundError from "./NotFoundError";
import FatalError from "./FatalError";
import "bootstrap/dist/js/bootstrap.bundle";

class DataCaterApp extends Component {
  render() {
    return (
      <BrowserRouter>
        <ErrorBoundary
          FallbackComponent={FatalError}
          onReset={() => {
            // reset the state of the app so the error doesn't happen again
          }}
        >
          <MainLayout>
            <Switch>
              <Route exact path="/" render={() => <Redirect to="/home" />} />

              <PrivateRoute exact path="/home" component={Home} />
              <Route
                exact
                path="/sign_in"
                render={(props) => <Login {...props} />}
              />

              <PrivateRoute
                exact
                path="/deployments"
                component={ListDeployments}
              />
              <PrivateRoute
                exact
                path="/deployments/new"
                component={NewDeployment}
              />
              <PrivateRoute
                exact
                path="/deployments/:id/edit"
                component={EditDeployment}
              />
              <PrivateRoute
                exact
                path="/deployments/:id/logs"
                component={DeploymentLogs}
              />
              <PrivateRoute
                exact
                path="/deployments/:id"
                component={ShowDeployment}
              />

              <PrivateRoute exact path="/pipelines" component={ListPipelines} />
              <PrivateRoute
                exact
                path="/pipelines/new"
                component={NewPipeline}
              />
              <PrivateRoute
                exact
                path="/pipelines/:id/settings"
                component={PipelineSettings}
              />
              <PrivateRoute
                exact
                path="/pipelines/:id/edit"
                component={EditPipeline}
              />
              <PrivateRoute
                exact
                path="/pipelines/:id"
                component={ShowPipeline}
              />

              <PrivateRoute exact path="/streams" component={ListStreams} />
              <PrivateRoute exact path="/streams/new" component={NewStream} />
              <PrivateRoute
                exact
                path="/streams/:id/inspect"
                component={InspectStream}
              />
              <PrivateRoute
                exact
                path="/streams/:id/edit"
                component={EditStream}
              />
              <PrivateRoute exact path="/streams/:id" component={ShowStream} />

              <PrivateRoute exact path="/configs" component={ListConfigs} />
              <PrivateRoute exact path="/configs/new" component={NewConfig} />
              <PrivateRoute exact path="/configs/:id/edit" component={EditConfig} />
              <PrivateRoute exact path="/configs/:id" component={ShowConfig} />

              <Route exact path="/404" component={NotFoundError} />
              <Redirect to="/404" />
            </Switch>
          </MainLayout>
        </ErrorBoundary>
      </BrowserRouter>
    );
  }
}

export default DataCaterApp;
