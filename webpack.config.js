const path = require('path');
const fs = require('fs');
const yaml = require('yaml');
const axios = require('axios');
const webpack = require('webpack');
const WebpackAssetsManifest = require('webpack-assets-manifest');
const IgnoreEmitPlugin = require('ignore-emit-webpack-plugin');
const MiniCssExtractPlugin = require('mini-css-extract-plugin');
const CssMinimizerPlugin = require('css-minimizer-webpack-plugin');
const TerserPlugin = require('terser-webpack-plugin');
const { ProvidePlugin } = require('webpack');

const scConfig = yaml.parse(fs.readFileSync(path.join(__dirname, 'config.yml'), {
    encoding: 'utf-8'
}));
const devMode = process.env.MODE === 'production' ? false : !!(scConfig.dev_mode && scConfig.dev_mode.enabled);
if (devMode) {
	console.log('RUNNING IN DEV MODE');
}

const BASE_PATH = path.join(__dirname, 'src', 'main', 'resources', 'web', 'public');
const BASE_PATH_BUILD = path.join(__dirname, 'target', 'classes', 'web', 'public');

const outBase = path.join(devMode ? BASE_PATH : BASE_PATH_BUILD, 'build');
const captchaBase = path.join(BASE_PATH, 'captcha');
const adminBase = path.join(BASE_PATH, 'admin');

function getFiles() {
    const files = fs.readdirSync(adminBase).filter(f => fs.statSync(path.join(adminBase, f)).isFile())
        .reduce((a, c) => {
            const f = c.split('.');
            f.splice(-1);
            a[`admin/${f}`] = path.join(adminBase, c).replaceAll('\\', '/');
            return a;
        }, {});
    return files;
}

let t = false;
const captchaFiles = {
    mode: devMode ? 'development' : 'production',
    entry: {
        captcha: path.join(captchaBase, 'captcha.tsx'),
        ...getFiles()
    },
    output: {
        path: path.join(outBase),
        filename: (p) => {
            if (p.chunk.name === 'captcha') {
                return 'captcha/captcha.js';
            }

            return '[name].[contenthash:8].js';
        },
        assetModuleFilename: (p) => {
            if (p.filename.endsWith('captcha.svg')) {
                return 'captcha/captcha.svg';
            }

            return '[name].[contenthash:8][ext]';
        },
        chunkFilename: '[name].[chunkhash:8].chunk.js',
        clean: true,
        chunkLoadingGlobal: 'snowCaptchaWebpackChunk'
    },
    cache: true,
    resolve: {
    	    extensions: [
                '.js',
                '.jsx',
                '.ts',
                '.tsx',
                '.d.ts'
    	    ],
    	    alias: {
                "react": "preact/compat",
                "react-dom/test-utils": "preact/test-utils",
                "react-dom": "preact/compat",     // Must be below test-utils
                "react/jsx-runtime": "preact/jsx-runtime"
    	    }
    	},
    	module: {
    		rules: [
    			{
    				test: /\.(j|t)sx?$/,
    				exclude: /\.d\.ts$/,
    				use: [
    					{
    					    loader: 'babel-loader'
                        }
    				]
    			},
    			{
                    test: /\.d\.ts$/,
                    loader: 'ignore-loader'
                },
    			{
                    test: /\.css$/,
                    use: [
                        MiniCssExtractPlugin.loader,
                        //'style-loader',
                        {
                            loader: 'css-loader',
                            options: {
                                modules: {
                                    localIdentName: 'sc_[hash:base64:12]'
                                }
                            }
                        },
                        'postcss-loader'
                    ]
                },
                {
                    test: /\.svg$/,
                    type: 'asset/resource'
                }
    		]
    	},
    	optimization: {
    		minimize: !devMode,
    		minimizer: [
    			new TerserPlugin({
    				test: /\.js$/,
    				extractComments: false,
    				terserOptions: {
    					format: {
    						comments: false
    					}
    				}
    			}),
    			new CssMinimizerPlugin({
    				include: /\.css$/,
    				minimizerOptions: {
    					preset: [
    						'default',
    						{
    							discardComments: {
    								removeAll: true
    							}
    						}
    					]
    				}
    			})
    		]
    	},
    	plugins: [
    		new ProvidePlugin({
    			React: devMode ? path.join(adminBase, '_common', '_preact-debug.js') : 'react'
    		}),
    		new MiniCssExtractPlugin({
    			filename: (p) => {
                    if (p.chunk.name === 'captcha') {
                        return 'captcha/captcha.css';
                    }

                    return '[name].[contenthash:8].css';
                }
    		}),
    		new WebpackAssetsManifest({
    		    done() {
    		        if (!devMode) return;
                    axios.get(`http://localhost:${scConfig.http.port}/_dev/reload`).then(() => {
                        console.log('Reloaded asset manifest');
                    }).catch(console.error);
    		    }
    		})
    	]
}

module.exports = [
    captchaFiles
];