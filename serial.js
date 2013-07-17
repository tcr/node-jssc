var keypress = require('keypress')
  , spawn = require('child_process').spawn
  , colors = require('colors');

function listen (port) {
  return spawn('java', ['-cp', './jSSC-2.6.0-Release/jssc.jar:.', 'Main', port], {
    cwd: __dirname
  });
}

function interactive (java) {
  java.stdout.on('data', function (data) {
    process.stdout.write(String(data).green);
  })
  java.stderr.pipe(process.stderr);

  // make `process.stdin` begin emitting "keypress" events
  keypress(process.stdin);

  // listen for the "keypress" event
  process.stdin.on('keypress', function (ch, key) {
    if (key && key.ctrl && key.name == 'c') {
      process.stdin.pause();
      java.kill('SIGHUP');
      return;
    }

    ch = ch.replace(/\r/, '\n')
    // process.stdout.write(ch.green);
    java.stdin.write(ch);
  });

  process.stdin.setRawMode(true);
  process.stdin.resume();
}

if (require.main == module) {
  interactive(listen(process.argv[2]));
}