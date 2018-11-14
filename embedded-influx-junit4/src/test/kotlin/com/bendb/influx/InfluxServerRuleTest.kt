package com.bendb.influx

import io.kotlintest.shouldBe
import org.influxdb.InfluxDBFactory
import org.junit.Rule
import org.junit.Test

class InfluxServerRuleTest {
    @get:Rule val serverRule = InfluxServerRule()

    @Test fun `rule starts an influx server`() {
        InfluxDBFactory.connect(serverRule.url).use { client ->
            client.ping()?.isGood shouldBe true
        }
    }
}