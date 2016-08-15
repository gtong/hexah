import Auth0Lock from 'auth0-lock';
import decode from 'jwt-decode';

const AUTH_KEY = "auth_token";
const REFRESH_KEY = "refresh_token";

class Auth {
  constructor(clientId, domain) {
    // Configure Auth0
    this.lock = new Auth0Lock(clientId, domain);
    this.refreshing = false;
    // binds login functions to keep this context
    this.handleAuthentication = this.handleAuthentication.bind(this);
    this.loggedIn = this.loggedIn.bind(this);
    this.withToken = this.withToken.bind(this);
  }

  handleAuthentication(err, token, refreshToken) {
    if (err == null) {
      localStorage.setItem(AUTH_KEY, token);
      localStorage.setItem(REFRESH_KEY, refreshToken);
    } else {
      this.logout();
    }
  }

  logout() {
    localStorage.removeItem(AUTH_KEY);
    localStorage.removeItem(REFRESH_KEY);
  }

  loggedIn() {
    let token = this.getToken();
    if (token) {
      if (this.isTokenExpired(token)) {
        // If we can still refresh the login, stay logged in
        // This checks to make sure that if we are in the middle of refreshing, we only call refreshLogin once
        // This is pretty messy code and needs refactoring
        return this.refreshing || this.refreshLogin();
      } else {
        return true;
      }
    } else {
      return false;
    }
  }

  withToken(callback) {
    let token = this.getToken();
    if (token) {
      if (this.isTokenExpired(token)) {
        this.refreshLogin(function() {
          // Once the refresh finishes, check the token again
          withToken(callback);
        });
      } else {
        callback(token);
      }
    } else {
      this.login(function() {
        if (this.loggedIn()) {
          callback(this.getToken());
        }
      });
    }
  }

  login(callback) {
    let that = this;
    this.lock.show({
      authParams: {
        scope: "openid offline_access"
      }
    }, function(err, profile, token, accessToken, state, refreshToken) {
      that.handleAuthentication(err, token, refreshToken);
      if (typeof callback === 'function') {
        callback();
      }
    });
  }

  getToken() {
    return localStorage.getItem(AUTH_KEY);
  }

  refreshLogin(callback) {
    let refreshToken = localStorage.getItem(REFRESH_KEY);
    let that = this
    if (refreshToken) {
      this.refreshing = true;
      this.lock.getClient().refreshToken(refreshToken, function(err, result) {
        that.refreshing = false;
        that.handleAuthentication(err, result.id_token, refreshToken);
        if (typeof callback === 'function') {
          callback();
        }
      });
      return true;
    } else {
      if (typeof callback === 'function') {
        callback();
      }
      return false;
    }
  }

  isTokenExpired(token) {
    const decoded = decode(token);
    if(!decoded.exp) {
      return true;
    }

    const expiration = new Date(0);
    expiration.setUTCSeconds(decoded.exp);

    const now = new Date();
    const offset = 5 * 60 * 1000; // Refresh the token if it's going to expire in 5 mins
    return (now.valueOf() + offset) > expiration.valueOf();
  }

}

export default new Auth("90IvedayHBwRS2OPVNFQwPt8t2lfajh6", "hexah.auth0.com");
