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

  loginCallback() {
    this.setState({
      loggedIn: auth.loggedIn()
    });
  }

  render() {
    if (this.state.loggedIn) {
      return (
        <div className="ui icon item">
          <Link to={'/profile'}><i className="user icon"></i></Link>
        </div>
      );
    } else {
      return (<a className="ui item" onClick={this.login}>Sign In</a>);
    }
  }
}

export default NavLogin;