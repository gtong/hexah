import React from "react";
import ReactDOM from 'react-dom';
import {Router, Route, hashHistory} from "react-router";
import App from "./components/App";

var router = (
  <Router history={hashHistory}>
    <Route path="/" component={App}/>
  </Router>
);

ReactDOM.render(router, document.getElementById("root"));
