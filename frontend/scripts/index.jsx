import "babel-polyfill";
import React from "react";
import ReactDOM from 'react-dom';
import {Router, Route, browserHistory} from "react-router";
import App from "./containers/App";
import ItemContainer from "./containers/ItemContainer";

var router = (
  <Router history={browserHistory}>
    <Route path="/" component={App}>
	  <Route name="item" path="/i/:nameKey" component={ItemContainer} />
	</Route>
  </Router>
);

ReactDOM.render(router, document.getElementById("root"));
