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

import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import org.influxdb.InfluxDBFactory
import org.junit.Rule
import org.junit.Test

class MultipleServerRulesTest {
    @get:Rule val serverOne = InfluxServerRule()
    @get:Rule val serverTwo = InfluxServerRule()

    @Test
    fun `multiple rules will start up distinct servers`() {
        serverOne.url shouldNotBe serverTwo.url

        InfluxDBFactory.connect(serverOne.url).use { client ->
            val pong = client.ping()
            pong.version shouldBe "1.7.0"
            pong.isGood shouldBe true
        }

        InfluxDBFactory.connect(serverTwo.url).use { client ->
            val pong = client.ping()
            pong.version shouldBe "1.7.0"
            pong.isGood shouldBe true
        }
    }
}