import React, {Component} from 'react';
import fetch from 'isomorphic-fetch';

class ItemSummary extends Component {
  render() {
    let summary = this.props.summary;
    return (
      <div>
        Foo
      </div>
    );
  }
}

ItemSummary.propTypes = {
  summary: React.PropTypes.object.isRequired
}

export default ItemSummary;
