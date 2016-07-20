import React, {Component} from 'react';
import { Link } from 'react-router'
import auth from '../utils/auth';

class NavLogin extends Component {
  constructor(props) {
    super(props);
    this.login = this.login.bind(this);
    this.loginCallback = this.loginCallback.bind(this);
    this.state = {
      loggedIn: auth.loggedIn()
    };
  }

  login() {
    auth.login(this.loginCallback);
  }

  loginCallback(err, profile, token) {
    this.setState({
      loggedIn: auth.loggedIn()
    });
  }

  render() {
    if (this.state.loggedIn) {
      return (<Link className="ui item" to={'/profile'}>Profile</Link>);
    } else {
      return (<a className="ui item" onClick={this.login}>Sign In</a>);
    }
  }
}

export default NavLogin;