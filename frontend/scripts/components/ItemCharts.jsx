import React, {Component} from 'react';
import map from 'lodash/map';
import fetch from 'isomorphic-fetch';
import {n} from '../util/format';

let Highcharts = require('highcharts/highstock');
require('highcharts/highcharts-more')(Highcharts);

function nChart() {
  return n(this.value);
}

class ItemCharts extends Component {
  constructor(props) {
    super(props);
    this.loadData = this.loadData.bind(this);
    this.updateCharts = this.updateCharts.bind(this);
    this.updateChart = this.updateChart.bind(this);
    this.createChart = this.createChart.bind(this);
    this.goldChart = null;
    this.platChart = null;
  }

  loadData(key) {
    fetch(`/api/feeds/${key}/`)
      .then(response => response.json())
      .then(json => {
        this.updateCharts(json);
      });
  }

  updateCharts(json) {
    this.platChart = this.updateChart('plat-chart', 'Platinum', 'p', this.platChart, json.platinum);
    this.goldChart = this.updateChart('gold-chart', 'Gold', 'g', this.goldChart, json.gold);
  }

  updateChart(id, currency, suffix, chart, data) {
    let medians = map(data, (v) => [v.d, v.m]);
    let ranges = map(data, (v) => [v.d, v.l, v.h]);
    let trades = map(data, (v) => [v.d, v.v]);
    if (chart !== null) {
      chart.series[0].setData(medians);
      chart.series[1].setData(ranges);
      chart.series[2].setData(trades);
    } else {
      chart = this.createChart(id, currency, suffix, medians, ranges, trades);
    }
    return chart;
  }

  createChart(id, currency, suffix, medians, ranges, trades) {
    return new Highcharts['stockChart'](id, {
      rangeSelector: { // Default 1m zoom
        selected: 0
      },
      tooltip: {
        shared: true
      },
      yAxis: [{ // Currency axis
        labels: {
          format: `{value}${suffix}`,
          formatter: nChart,
          style: {color: Highcharts.getOptions().colors[0]}
        },
        title: {
          text: currency,
          style: {color: Highcharts.getOptions().colors[0]}
        },
        min: 0,
        opposite: false // StockChart opposite is reversed
      }, {
        title: { // Trades axis
          text: 'Trades',
          style: {color: Highcharts.getOptions().colors[1]}
        },
        labels: {
          formatter: nChart,
          style: {color: Highcharts.getOptions().colors[1]}
        },
        min: 0,
        opposite: true
      }],
      series: [{
        name: 'Median',
        data : medians,
        lineWidth: 1.5,
        zIndex: 2,
        tooltip: {valueSuffix: suffix}
      },{
        name: 'Range',
        data: ranges,
        type: 'arearange',
        lineWidth: 0.5,
        linkedTo: ':previous',
        color: Highcharts.getOptions().colors[0],
        fillOpacity: 0.3,
        zIndex: 1,
        tooltip: {valueSuffix: suffix}
      }, {
        name: 'Trades',
        type: 'column',
        yAxis: 1,
        zIndex: 0,
        data: trades,
        color: '#ccc'
      }]
    });
  }

  componentDidMount() {
    this.loadData(this.props.nameKey);
  }

  componentWillReceiveProps(props) {
    if (this.props.nameKey !== props.nameKey) {
      this.loadData(props.nameKey)
    }
  }

  componentWillUnmount() {
    if (this.goldChart) {
      this.goldChart.destroy();
    }
    if (this.platChart) {
      this.platChart.destroy();
    }
  }

  render() {
    return (
      <div className="sixteen wide column">
        <div className="ui one column grid">
          <div className="column">
            <h5 className="ui top attached header">Platinum</h5>
            <div id="plat-chart" className="ui bottom attached segment"></div>
          </div>

          <div className="column">
            <h5 className="ui top attached header">Gold</h5>
            <div id="gold-chart" className="ui bottom attached segment"></div>
          </div>
        </div>
      </div>
    );
  }

}

ItemCharts.propTypes = {
  nameKey: React.PropTypes.string.isRequired,
}

export default ItemCharts;
