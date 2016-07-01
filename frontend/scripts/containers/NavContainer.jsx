import React, {Component} from 'react';
import NavSearch from "../components/NavSearch";
import NavLogin from "../components/NavLogin";

require("./styles/nav.scss");

class NavContainer extends Component {
  render() {
    return (
      <div id="nav" className="ui fixed inverted grey menu">
        <a href="/" className="header item">Hexah.io</a>
        <NavSearch/>
      </div>
    );
  }
}

export default NavContainer;