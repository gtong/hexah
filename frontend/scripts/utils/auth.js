import Auth0Lock from 'auth0-lock'

const AUTH_KEY = "auth_token";

class Auth {
  constructor(clientId, domain) {
    // Configure Auth0
    this.lock = new Auth0Lock(clientId, domain, {})
    // Add callback for lock `authenticated` event
    this.lock.on('authenticated', this.handleAuthentication.bind(this))
    // binds login functions to keep this context
    this.login = this.login.bind(this)
  }

  handleAuthentication(authResult) {
  	localStorage.setItem(AUTH_KEY, authResult.idToken)
  }

  loggedIn() {
    return !!this.getToken()
  }

  login(callback) {
    this.lock.show({
      rememberLastLogin: true
    }, callback)
  }

  getToken() {
    return localStorage.getItem(AUTH_KEY)
  }

  logout() {
    localStorage.removeItem(AUTH_KEY);
  }
}

export default new Auth("90IvedayHBwRS2OPVNFQwPt8t2lfajh6", "hexah.auth0.com");
