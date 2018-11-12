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

import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.influxdb.InfluxDBFactory
import org.junit.Assert.assertThat
import org.junit.Test
import java.io.File
import java.util.function.Supplier

class InfluxServerTest {
    @Test fun `builder default port is 8086`() {
        assertThat(InfluxServerBuilder().port, CoreMatchers.equalTo(8086))
    }

    @Test fun `builder binary supplier is BinaryProvider by default`() {
        assertThat(InfluxServerBuilder().executableSupplier, CoreMatchers.sameInstance<Supplier<File>>(BinaryProvider))
    }

    @Test fun `start starts an InfluxDB server`() {
        val server = InfluxServer(8086).apply {
            start()
        }

        try {
            val client = InfluxDBFactory.connect("http://127.0.0.1:8086")
            assertThat(client.ping(), `is`(notNullValue()))
            client.close()
        } finally {
            server.close()
        }

    }
}