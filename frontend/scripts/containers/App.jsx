import React, {Component} from 'react';
import NavContainer from "./NavContainer";

class App extends Component {
  render() {
    return (
      <div>
        <NavContainer/>
        {this.props.children}
      </div>
    );
  }
}
export default App;