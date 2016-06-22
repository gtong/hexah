import React, {Component} from 'react';
import ItemSummary from '../components/ItemSummary';

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
    return (
      <div id="main" className="ui grid container">
        <div className="row">
	      <h1 className="ui dividing header column">{summary.name}</h1>
	    </div>
        <ItemSummary summary={summary}/>
      </div>
    );
  }
}

ItemContainer.propTypes = {
  params: React.PropTypes.object.isRequired
}

export default ItemContainer;