import React, {Component} from 'react';
import keyBy from 'lodash/keyBy';
import fetch from 'isomorphic-fetch';

class Search extends Component {
  constructor(props) {
    super(props);
    this.loadSearch = this.loadSearch.bind(this);
    this.selectValue = this.selectValue.bind(this);
    this.changeValue = this.changeValue.bind(this);
    this.state = {
      value: '',
      items: [],
      lookup: {},
    };
  }

  loadSearch() {
    fetch('/api/objects/')
      .then(response => response.json())
      .then(json => {
        this.setState({
          items: json,
          lookup: keyBy(json, 'name'),
        });
        $('#search').search({
          source: this.state.items,
          fields: {title: 'name'},
          searchFields: ['name'],
          onSelect: function(item) {
            this.selectValue(item.name);
          }.bind(this)
        });
      });
  }

  componentDidMount() {
    this.loadSearch();
  }

  selectValue(value) {
    if (value in this.state.lookup) {
      this.context.router.push("/i/" + this.state.lookup[value].id);
    }
  }

  changeValue(event) {
    let value = event.target.value;
    this.setState({value: value});
    this.selectValue(value);
  }

  render() {
    return (
      <div id="search" className="ui search item fourteen wide column">
        <div className="ui icon input">
          <input
            className="prompt" type="text" placeholder="Search..."
            value={this.state.value} onChange={this.changeValue}
          />
          <i className="search link icon"></i>
        </div>
        <div className="results"></div>
      </div>
    );
  }
  
}

Search.contextTypes = {
  router: React.PropTypes.object.isRequired
};

export default Search;
