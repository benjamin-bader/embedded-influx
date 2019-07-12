# embedded-influx
### Embedded InfluxDB Server for Tests

[![Build Status](https://travis-ci.org/benjamin-bader/embedded-influx.svg?branch=master)](https://travis-ci.org/benjamin-bader/embedded-influx)

The currently bundled version of influxd is 1.7.0. 

Add to your project like so:

```gradle
dependencies {
  // If you just want to use InfluxServer manually:
  testImplementation 'com.bendb.influx:embedded-influx:0.2.0'

  // Or...
  // If you want to use the JUnit 4 InfluxServerRule:
  testImplementation 'com.bendb.influx:embedded-influx-junit4:0.2.0'

  // Or...
  // If you want a JUnit 5 Extension:
  testImplementation 'com.bendb.influx:embedded-influx-junit5:0.2.0'
}
```

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

### JUnit 4

We ship a `@Rule` that starts a local server before each test, and stops it afterwards:

```kotlin

class SomeTest {
  @get:Rule val serverRule = InfluxServerRule()

  @Test fun serverIsUp() {
    InfluxDBFactory.connect(serverRule.url).use { client ->
      val pong = client.ping()
      pong?.isGood shouldBe true
    }
  }
}

```


### JUnit 5

We also ship a JUnit 5 extension, which does the same thing as the rule described above:

```kotlin
@ExtendWith(InfluxServerExtension::class)
class AnotherTest {
  private lateinit var server: InfluxServer
  
  @Test fun serverIsUp() {
    InfluxDBFactory.connect(serverRule.url).use { client ->
      val pong = client.ping()
      pong?.isGood shouldBe true
    }
  }
}
```


Copyright 2018-2019 Benjamin Bader
Released under the Apache 2.0 License
