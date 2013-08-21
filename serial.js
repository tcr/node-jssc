var keypress = require('keypress')
  , spawn = require('child_process').spawn
  , colors = require('colors')
  , carrier = require('carrier')
  , path = require('path');

function listen (port, debug) {
  var java = spawn('java', ['-cp', './jSSC-2.6.0-Release/jssc.jar' + path.delimiter + '.', 'Main', port], {
    cwd: __dirname
  });
  java.debug = debug;
  carrier.carry(java.stdout, function (data) {
    var str = String(data);
    var payload = str.length > 1 ? new Buffer(str.substr(1), 'base64').toString('utf-8') : '';
    if (java.debug) {
      process.stdout.write(str[0].green + ' ' + new Buffer(str.substr(1), 'base64').toString('utf-8'));
    }
    switch (str[0]) {
      case 'L': java.emit('connected'); return;
      case 'E': java.emit('error', payload); return;
      case 'I': java.emit('data', payload); return;
      // clear to send
      case 'C': java.emit('cts', true); return;
      case 'c': java.emit('cts', false); return;
      // data set ready
      case 'D': java.emit('dsr', true); return;
      case 'd': java.emit('dsr', false); return;
      // available ports
      case 'p': java.emit('port', payload); return;
    }
  })
  java.send = function (type, data, next) {
    if (java.debug) {
      console.log(type.green, String(data).yellow);
    }
    java.stdin.write(type + (Buffer.isBuffer(data) ? data : new Buffer(data)).toString('base64') + '\n', next);
  };
  java.write = function (data, next) {
    java.send('O', data, next);
  };

  return java;
}

function interactive (java) {
  java.stderr.pipe(process.stderr);
  java.on('close', function (code) {
    console.log('jssc exited with code', code);
    process.exit(1);
  })
  java.on('connected', function (code) {
    console.log('jssc connected');
  })

  // make `process.stdin` begin emitting "keypress" events
  keypress(process.stdin);

  // listen for the "keypress" event
  process.stdin.on('keypress', function (ch, key) {
    if (key && key.ctrl && key.name == 'c') {
      process.stdin.pause();
      java.kill();
      return;
    }

    ch = ch.replace(/\r/, '\n')
    java.write(ch);
  });

  process.stdin.setRawMode(true);
  process.stdin.resume();
}

exports.listen = listen;
exports.interactive = interactive;

if (require.main == module) {
  interactive(listen(process.argv[2], true));
}