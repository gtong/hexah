import React, {Component} from 'react';
import NavSearch from "../components/NavSearch";
import NavLogin from "../components/NavLogin";

require("./styles/nav.scss");

class NavContainer extends Component {
  render() {
    return (
      <div id="nav" className="ui fixed grey inverted menu">
        <div className="ui grid container">
          <a href="/" className="header two wide column item">Hexah.io</a>
          <NavSearch/>
          <NavLogin/>
        </div>
      </div>
    );
  }
}

export default NavContainer;