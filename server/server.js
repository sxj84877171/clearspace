var cluster = require('cluster');
var util = require('util');
var express = require('express');
var morgan = require('morgan');
var http = require('http');
var servestatic = require('serve-static');
var validator = require('validator');
var merge = require('merge');
var bodyParser = require('body-parser');

// 暂时只启动一个线程,多个线程数据不能共享,以后再改. [2015-6-15]
var workers = 1; //process.env.WORKERS || require('os').cpus().length;

if (cluster.isMaster) {
    startMaster();
} else {
    startSlaver();
}

function startMaster() {
    console.log('start cluster with workers, ', workers);

    for (var i = 0; i < workers; ++i) {
        var worker = cluster.fork().process;
        console.log('worker %s started.', worker.pid);
    }

    cluster.on('exit', function (worker) {
        console.log('worker %s died. restart...', worker.process.pid);
        cluster.fork();
    });
}

function startSlaver() {
    console.log('start slave');

    var app = new express();

    app.use(servestatic(__dirname + "/web", { 'index': ['index.html'] }));

    app.use(bodyParser.urlencoded({ extended: false }))
    app.use(bodyParser.json());

    var server = http.createServer(app);
    server.listen(80);
}

process.on('uncaughtException', function (err) {
	try{
		console.log(' uncaughtException:', err.message)
		console.log(err.stack)
		console.log("global uncaughtException")
		process.exit(1)
    } catch (e) {
		console.log('** exception:', e.stack);
	}
})
