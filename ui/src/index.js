import React from "react";
import ReactDOM from "react-dom";
import { createStore, applyMiddleware } from "redux";
import { Provider } from "react-redux";
import thunk from "redux-thunk";
import rootReducer from "./reducers";
import DataCaterApp from "./containers/DataCaterApp";
import "./scss/main.scss";

const store = createStore(rootReducer, applyMiddleware(thunk));

ReactDOM.render(
  <Provider store={store}>
    <DataCaterApp />
  </Provider>,
  document.getElementById("root")
);
