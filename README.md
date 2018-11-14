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


Copyright 2018 Benjamin Bader
Released under the Apache 2.0 License