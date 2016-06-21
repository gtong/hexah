import React from "react";
import ReactDOM from 'react-dom';
import {Router, Route, browserHistory} from "react-router";
import App from "./containers/App";
import ItemContainer from "./containers/ItemContainer";

var router = (
  <Router history={browserHistory}>
    <Route path="/" component={App}>
	  <Route name="item" path="/i/:item" component={ItemContainer} />
	</Route>
  </Router>
);

ReactDOM.render(router, document.getElementById("root"));
