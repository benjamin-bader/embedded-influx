# embedded-influx
### Embedded InfluxDB Server for Tests

[![Build Status](https://travis-ci.org/benjamin-bader/embedded-influx.svg?branch=master)](https://travis-ci.org/benjamin-bader/embedded-influx)

Use like so:

```kotlin
val server = InfluxServer.builder()
        .port(8086)
        .build()
        .start()


val client = InfluxDBFactory.connect(server.url)

client.ping() // etc

// When you're done, shut it down
server.close()

```

Copyright 2018 Benjamin Bader
Released under the Apache 2.0 License