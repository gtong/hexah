import React from 'react';
import SearchComponent from "./SearchComponent";

export default class App extends React.Component {
  render() {
    return (
      <div className="ui fixed large menu">
        <div className="ui two column grid container" style={{margin: 0}}>
          <a href="/" className="header two wide column item">Hexah.io</a>
          <SearchComponent/>
        </div>
      </div>
    );
  }
}
