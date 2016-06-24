import React, {Component} from 'react';
import isEmpty from 'lodash/isEmpty';
import ItemSummary from '../components/ItemSummary';
import ItemCharts from '../components/ItemCharts';

require('./styles/main.scss');

class ItemContainer extends Component {
  constructor(props) {
    super(props);
    this.loadSummary = this.loadSummary.bind(this);
    this.state = {
      summary: {}
    };
  }

  componentDidMount() {
    this.loadSummary(this.props.params.nameKey);
  }

  componentWillReceiveProps(props) {
    if (this.props.params.nameKey !== props.params.nameKey) {
      this.loadSummary(props.params.nameKey)
    }
  }

  loadSummary(nameKey) {
    fetch(`/api/feeds/${nameKey}/summary`)
      .then(response => response.json())
      .then(json => {
        this.setState({summary: json});
      });
  }

  render() {
    let summary = this.state.summary;
    if (isEmpty(summary)) {
      return (
        <div id="main" className="ui grid container"></div>
      )
    } else {
      return (
        <div id="main" className="ui grid container">
          <h1 className="ui dividing header sixteen wide column">{summary.name}</h1>
          <ItemSummary summary={summary}/>
          <ItemCharts nameKey={this.props.params.nameKey}/>
        </div>
      );
    }
  }
}

ItemContainer.propTypes = {
  params: React.PropTypes.object.isRequired
}

export default ItemContainer;