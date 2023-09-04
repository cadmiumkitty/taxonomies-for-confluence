const webpack = require('webpack');
module.exports = {
    context: __dirname + '/src/main/javascript',
    entry:{
        'resource-macro-editor': './resource-macro-editor.js',
        'relation-macro-editor': './relation-macro-editor.js',
        'toc-macro-editor': './toc-macro-editor.js',
        'sparql-macro-editor': './sparql-macro-editor.js',
        'subject-byline': './subject-byline.js',
        'type-byline': './type-byline.js',
        'class-byline': './class-byline.js',
        'taxonomy-admin-page': './taxonomy-admin-page.js',
        'taxonomy-page': './taxonomy-page.js'
    },    
    output: {
        path: __dirname + '/target/classes/static/js',
        filename: '[name].js'
    },
    module: {
        rules: [
            {
                test: /\.(js|jsx)$/,
                exclude: /node_modules/,
                use: ['babel-loader']
            },
            {
                test: /\.css$/,
                use: ['style-loader', 'css-loader']
            },
            {
                test: /\.less$/,
                use: ['less-loader']
            }
        ]
    },
    plugins: [
        new webpack.ProvidePlugin({
            process: 'process/browser',
        })
    ]
};