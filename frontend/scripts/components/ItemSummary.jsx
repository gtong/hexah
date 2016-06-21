import React, {Component} from 'react';

class ItemSummary extends Component {
  constructor(props) {
    super(props);
    this.loadSummary = this.loadSummary.bind(this);
    this.state = {
      summary: {}
    };
  }

  componentDidMount() {
    this.loadSummary();
  }

  loadSummary() {
    $.getJSON(`/api/feeds/${this.props.nameKey}/summary`, function(json) {
      this.setState({
        summary: json
      });
    }.bind(this));
  }

  render() {
    let summary = this.state.summary;
    return (
      <div>
        <h1>{summary.name}</h1>
      </div>
    );
  }
  
}

ItemSummary.propTypes = {
  nameKey: React.PropTypes.string.isRequired
}

export default ItemSummary;
