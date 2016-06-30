import React, {Component} from 'react';
import auth from '../utils/auth';

class NavLogin extends Component {
  constructor(props) {
    super(props);
    this.login = this.login.bind(this);
    this.state = {
    };
  }

  login() {
    auth.login();
  }

  render() {
    return (
      <a className="ui item" onClick={this.login}>Sign In</a>
    );
  }
}

export default NavLogin;