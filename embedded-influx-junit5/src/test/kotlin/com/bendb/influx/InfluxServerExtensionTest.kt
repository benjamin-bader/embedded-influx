package com.bendb.influx

import io.kotlintest.matchers.types.shouldBeSameInstanceAs
import io.kotlintest.shouldBe
import org.influxdb.InfluxDBFactory
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(InfluxServerExtension::class)
class InfluxServerExtensionTest {
    private lateinit var server: InfluxServer
    private lateinit var otherServer: InfluxServer

    @Test fun serverIsInjectedAndRunning() {
        InfluxDBFactory.connect(server.url).use { client ->
            val pong = client.ping()
            pong?.isGood shouldBe true
        }
    }

    @Test fun onlyOneServerInstanceIsInjected() {
        server.shouldBeSameInstanceAs(otherServer)
    }
}