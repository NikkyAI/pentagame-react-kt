config.devServer = config.devServer || {}; // create devServer in case it is undefined
config.devServer.watchOptions = {
    "aggregateTimeout": 5000,
    "poll": 1000
};