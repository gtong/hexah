import React, {Component} from 'react';
import fetch from 'isomorphic-fetch';
import {n, f} from '../util/format';

class ItemSummary extends Component {

  render() {
    let summary = this.props.summary;
    let image = `http://hexah-images.s3-website-us-west-1.amazonaws.com/big/${summary.key}.jpg`;
    let row = function(name, key, format, currency = false) {
      let ps = currency ? 'p' : '';
      let gs = currency ? 'g' : '';
      return (
        <tr>
          <td>{name}</td>
          <td className="right aligned">{format(summary.platinum[key])}{ps}</td>
          <td className="right aligned">{format(summary.gold[key])}{gs}</td>
        </tr>
      )
    }
    return (
      <div className="row">
        <div className="four wide column">
          <img className="ui fluid image" src={image}/>
        </div>

        <div className="four wide column">
          <table className="ui table">
            <thead>
              <tr><th colSpan="3">Most Recent</th></tr>
            </thead>
            <tbody>
              {row('Median', 'recent_median', n, true)}
              {row('Trades', 'recent_trades', n)}
            </tbody>
          </table>
        </div>

        <div className="four wide column">
          <table className="ui table">
            <thead>
              <tr><th colSpan="3">Last 7 Days</th></tr>
            </thead>
            <tbody>
              {row('Average median', 'last_7_average_median', n, true)}
              {row('Trades', 'last_7_trades', n)}
              {row('Trades/day', 'last_7_trades_per_day', f)}
            </tbody>
          </table>
        </div>

        <div className="four wide column">
          <table className="ui table">
            <thead>
              <tr><th colSpan="3">Overall</th></tr>
            </thead>
            <tbody>
              {row('Average median', 'total_average_median', n, true)}
              {row('Trades', 'total_trades', n)}
              {row('Trades/day', 'total_trades_per_day', f)}
            </tbody>
          </table>
        </div>

      </div>
    );
  }

}

ItemSummary.propTypes = {
  summary: React.PropTypes.object.isRequired
}

export default ItemSummary;
