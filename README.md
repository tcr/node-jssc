# Java(Script) Serial Connector

**[jssc](https://code.google.com/p/java-simple-serial-connector/), Java Simple Serial Connector, ported to Node.js.**

Install:

```
npm install jssc
```

Use:

```
var jssc = require('jssc');

var serial = jssc.listen(path[, debugmode]);

// writing data
serial.write(buffer, function (err) { ... });

// events
serial.on('connected', function (err) { ... })
serial.on('data', function (err) { ... })
serial.on('error', function (err) { ... })
serial.on('close', function (err) { ... })

// serial line controls
serial.on('cts', function (isClearToSend) { ... })
serial.on('dsr', function (isDataSetReady) { ... })
```

## Requirements

* Java
* courage