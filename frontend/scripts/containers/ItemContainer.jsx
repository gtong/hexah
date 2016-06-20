import React, {Component} from 'react';

require('./styles/main.scss');

class ItemContainer extends Component {
  render() {
    return (
      <div id="main" className="ui container">
        <h1>{this.props.params.itemName}</h1>
      </div>
    );
  }
}
export default ItemContainer;