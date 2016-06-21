import React, {Component} from 'react';
import find from 'lodash/find';

class Search extends Component {
  constructor(props) {
    super(props);
    this.state = {
      items: [],
    };
  }

  componentDidMount() {
    $.getJSON('/api/objects/', function(json) {
      this.setState({
        items: json
      });
      $('#search').search({
        source: this.state.items,
        fields: {
          title: 'name'
        },
        searchFields: ['name'],
        onSelect: function(item) {
          this.setValue(item.name);
        }.bind(this)
      });
    }.bind(this));
  }

  setValue(value) {
    let item = find(this.state.items, ['name', value]);
    if (item) {
      this.context.router.push("/i/" + item.id);
    }
  }

  render() {
    return (
      <div id="search" className="ui search item fourteen wide column">
        <div className="ui icon input">
          <input className="prompt" type="text" placeholder="Search..."/>
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
