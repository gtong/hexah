import React, {Component} from 'react';
import isEmpty from 'lodash/isEmpty';
import auth from '../utils/auth';

class ProfileContainer extends Component {
  constructor(props) {
    super(props);
    this.loadProfile = this.loadProfile.bind(this);
    this.state = {
      profile: {}
    };
  }

  componentDidMount() {
    this.loadProfile();
  }

  loadProfile() {
    if (auth.loggedIn()) {
      fetch('/api/my/profile', {method: 'GET', headers: {'token': auth.getToken()}})
        .then(response => {
          if (response.status != 200) {
            throw new Error("Error from server");
          }
          return response.json()
        })
        .then(json => {
          this.setState({profile: json});
        });
    }
  }

  formRow(label, element) {
    return (
      <div className="field">
        <div className="two fields">
          <div className="three wide field"><label>{label}</label></div>
          <div className="thirteen wide field">{element}</div>
        </div>
      </div>
    );
  }

  render() {
    let profile = this.state.profile;
    if (isEmpty(profile)) {
      return (
        <div id="main" className="ui grid container"></div>
      )
    } else {
      return (
        <div id="main" className="ui grid container">
          <h1 className="ui dividing header sixteen wide column">Account</h1>
          <div className="eight wide column">
            <form className="ui form">
              {this.formRow('Email', <span>{profile.email}</span>)}
              {this.formRow('ID', <span>{profile.guid}</span>)}
              {this.formRow('Status', <span>{profile.status}</span>)}
              {this.formRow('Sell Cards', <span>{profile.sellCards ? 'yes' : 'no'}</span>)}
              {this.formRow('Sell Equipment', <span>{profile.sellEquipment ? 'yes' : 'no'}</span>)}
            </form>
          </div>
        </div>
      );
    }
  }
}

export default ProfileContainer;