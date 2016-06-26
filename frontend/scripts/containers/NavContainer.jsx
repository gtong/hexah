import React, {Component} from 'react';
import Search from "../components/Search";

require("./styles/nav.scss");

class NavContainer extends Component {
  render() {
    return (
      <div id="nav" className="ui fixed grey inverted large menu">
        <div className="ui two column grid container">
          <a href="/" className="header two wide column item">Hexah.io</a>
          <Search/>
        </div>
      </div>
    );
  }
}

export default NavContainer;