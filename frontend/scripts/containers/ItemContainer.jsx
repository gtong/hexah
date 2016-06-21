import React, {Component} from 'react';
import ItemSummary from '../components/ItemSummary';

require('./styles/main.scss');

class ItemContainer extends Component {
  render() {
    let params = this.props.params;
    return (
      <div id="main" className="ui container">
        <ItemSummary nameKey={params.item}/>
      </div>
    );
  }
}
export default ItemContainer;