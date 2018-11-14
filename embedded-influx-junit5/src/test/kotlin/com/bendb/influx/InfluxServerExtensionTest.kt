package com.bendb.influx

import org.influxdb.InfluxDBFactory
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.function.Executable

@ExtendWith(InfluxServerExtension::class)
class InfluxServerExtensionTest {
    private lateinit var server: InfluxServer
    private lateinit var otherServer: InfluxServer

    @Test fun serverIsInjectedAndRunning() {
        InfluxDBFactory.connect(server.url).use { client ->
            val pong = client.ping()
            Assertions.assertAll(
                Executable { Assertions.assertNotNull(pong) },
                Executable { Assertions.assertTrue(pong.isGood) }
            )
            Assertions.assertNotNull(client.ping())
        }
    }

    @Test fun onlyOneServerInstanceIsInjected() {
        Assertions.assertSame(server, otherServer)
    }
}