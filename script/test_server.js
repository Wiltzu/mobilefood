var express = require('express');
var fs = require('fs');

var app = express();

var counter = 0;

app.get('/mobilerest/', function (req, res) {
	filepath = 'unica_test.json';

	fs.readFile(filepath, 'utf8', function (err, data) {
		if(err) {
			console.log(err);
			res.type('text/plain');
			res.send('ERROR 404: file not found');
			return;
		}
		res.type('text/json');
		res.send(data);
	});

	console.log('Client queried for: ' + JSON.stringify(req.query));
	console.log('Answered to request #' + (++counter));
});

port = process.env.PORT || 4731;

console.log("Listening on port " + port);

app.listen(port);
