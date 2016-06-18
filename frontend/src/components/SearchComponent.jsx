import React from 'react';

var SearchComponent = React.createClass({

  getInitialState() {
      return {
          search: []
      };
  },

  componentDidMount() {
    $.getJSON('/api/objects/', function(json) {
      this.setState({
        search: json
      });
      $('#search').search({
        source: this.state.search,
        fields: {
          title: 'name'
        },
        searchFields: ['name'],
      });
    }.bind(this));
  },

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

});

export default SearchComponent;
