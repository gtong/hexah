import Auth0Lock from 'auth0-lock'

const AUTH_KEY = "auth_token";

class Auth {
  constructor(clientId, domain) {
    // Configure Auth0
    this.lock = new Auth0Lock(clientId, domain, {})
    // binds login functions to keep this context
    this.login = this.login.bind(this)
    this.handleAuthentication = this.handleAuthentication.bind(this)
  }

  handleAuthentication(err, profile, token) {
    if (err == null) {
      localStorage.setItem(AUTH_KEY, token)
    }
  }

  loggedIn() {
    return !!this.getToken()
  }

  login(callback) {
    let that = this
    this.lock.show({
      rememberLastLogin: true
    }, function(err, profile, token) {
      that.handleAuthentication(err, profile, token);
      if (typeof callback === 'function') {
        callback(err, profile, token);
      }
    });
  }

  getToken() {
    return localStorage.getItem(AUTH_KEY)
  }

  logout() {
    localStorage.removeItem(AUTH_KEY);
  }
}

export default new Auth("90IvedayHBwRS2OPVNFQwPt8t2lfajh6", "hexah.auth0.com");
