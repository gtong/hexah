var path = require('path');
var webpack = require('webpack');
var ExtractTextPlugin = require("extract-text-webpack-plugin");

var scriptsDir = path.resolve(__dirname, 'scripts');

module.exports = {
  entry: {
    main: './scripts/index',
    vendor: [
      'babel-polyfill',
      'highcharts',
      'isomorphic-fetch',
      'lodash',
      'numeral',
      'react',
      'react-dom',
      'react-router',
    ]
  },
  output: {
    publicPath: '/static/',
    path: path.join(__dirname, 'dist'),
    filename: 'main.js'
  },
  resolve: {
    extensions: ["", ".jsx", ".js"],
    modulesDirectories: ["src", "node_modules"]
  },
  module: {
    loaders: [
      { test: /\.jsx?$/, loader: 'babel?' + JSON.stringify({presets: ['react', 'es2015']}), include: scriptsDir },
      { test: /\.scss$/, loader: ExtractTextPlugin.extract('style', 'css!sass') },
      // Needed by Auth0
      { test: /node_modules[\\\/]auth0-lock[\\\/].*\.js$/, loaders: ['transform-loader/cacheable?brfs', 'transform-loader/cacheable?packageify'] },
      { test: /node_modules[\\\/]auth0-lock[\\\/].*\.ejs$/, loader: 'transform-loader/cacheable?ejsify'},
      { test: /\.json$/, loader: 'json-loader' }
    ]
  },
  plugins: [
    new webpack.DefinePlugin({
      'process.env': {
        'NODE_ENV': JSON.stringify('production')
      }
    }),
    new webpack.optimize.UglifyJsPlugin({
      compress: {
        warnings: false
      },
    }),
    new ExtractTextPlugin("main.css"),
    new webpack.optimize.CommonsChunkPlugin('vendor', 'vendor.js')
  ]
};
