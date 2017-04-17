var path = require("path");
var webpack = require("webpack");

module.exports = {
    resolve: {
        modulesDirectories: ["node_modules", "bower_components"]
    },
    plugins: [
        new webpack.ResolverPlugin(
            new webpack.ResolverPlugin.DirectoryDescriptionFilePlugin(".bower.json", ["main"])
        )
    ]
}