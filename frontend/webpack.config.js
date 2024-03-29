var path = require('path');
var webpack = require('webpack');

var scriptsDir = path.resolve(__dirname, 'scripts');

module.exports = {
  devtool: 'eval',
  entry: [
    'webpack-dev-server/client?http://localhost:3000',
    'webpack/hot/only-dev-server',
    './scripts/index'
  ],
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
      { test: /\.jsx?$/, loaders: ['react-hot', 'babel?' + JSON.stringify({presets: ['react', 'es2015']})], include: scriptsDir },
      { test: /\.scss$/, loaders: ['style', 'css', 'sass'] },
      // Needed by Auth0
      { test: /node_modules[\\\/]auth0-lock[\\\/].*\.js$/, loaders: ['transform-loader/cacheable?brfs', 'transform-loader/cacheable?packageify'] },
      { test: /node_modules[\\\/]auth0-lock[\\\/].*\.ejs$/, loader: 'transform-loader/cacheable?ejsify'},
      { test: /\.json$/, loader: 'json-loader' }
    ]
  },
  devServer: {
    host: '0.0.0.0',
    proxy: {
      '/api/*': 'http://localhost:8080',
    }
  }
};
