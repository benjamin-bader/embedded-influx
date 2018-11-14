/*
 * Copyright 2018 Benjamin Bader.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.bendb.influx

import io.kotlintest.matchers.types.shouldBeSameInstanceAs
import io.kotlintest.shouldBe
import org.influxdb.InfluxDBFactory
import org.junit.Test

class InfluxServerTest {
    @Test fun `builder default port is 8086`() {
        InfluxServerBuilder().port shouldBe 8086
    }

    @Test fun `builder binary supplier is BinaryProvider by default`() {
        InfluxServerBuilder().executableSupplier.shouldBeSameInstanceAs(BinaryProvider)
    }

    @Test fun `start starts an InfluxDB server`() {
        InfluxServer(8086).use { server ->
            server.start()

            InfluxDBFactory.connect("http://127.0.0.1:8086").use { client ->
                client.ping()?.isGood shouldBe true
            }
        }
    }
}