import React from "react";
import { Route, Redirect } from "react-router-dom";

export const PrivateRoute = ({ component: Component, ...rest }) => (
  <Route
    {...rest}
    render={(props) =>
      ![null, undefined, "undefined"].includes(
        localStorage.getItem("userToken")
      ) ? (
        <Component {...props} />
      ) : (
        <Redirect
          to={{ pathname: "/sign_in", state: { from: props.location } }}
        />
      )
    }
  />
);
