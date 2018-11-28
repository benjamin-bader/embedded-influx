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
import org.influxdb.dto.BatchPoints
import org.influxdb.dto.Point
import org.influxdb.dto.Query
import org.junit.Test
import java.util.concurrent.TimeUnit

class InfluxServerTest {
    @Test fun `builder default port is "undefined"`() {
        InfluxServerBuilder().port shouldBe 0
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

    @Test fun `data written to one server will not be readable from another`() {
        InfluxServer().use { server ->
            server.start()

            InfluxDBFactory.connect(server.url).use { client ->
                client.createDatabase("sample")
                client.createRetentionPolicy("default", "sample", "30d", 1, true)

                val point = Point.measurement("memory")
                    .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                    .addField("a", 1L)
                    .addField("b", 2L)
                    .build()
                client.write(BatchPoints.database("sample").point(point).build())

                val result = client.query(Query("select * from memory", "sample"))
                result.results.size shouldBe 1

                val series = result.results[0]
                series.hasError() shouldBe false
                series.series[0].name shouldBe "memory"
            }
        }

        InfluxServer().use { server ->
            server.start()
            InfluxDBFactory.connect(server.url).use { client ->
                val result = client.query(Query("select * from memory", "sample"))
                result.results.size shouldBe 1

                val series = result.results[0]
                series.hasError() shouldBe true
            }
        }
    }
}