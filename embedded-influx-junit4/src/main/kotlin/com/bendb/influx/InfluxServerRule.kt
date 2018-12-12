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

import org.junit.rules.ExternalResource
import java.time.Duration

/**
 * A JUnit 4 `@Rule` that creates and starts an [InfluxServer] before and
 * after each test in the class, respectively.
 *
 * Example usage:
 *
 * ```
 * class ExampleTest {
 *   @get:Rule val serverRule = InfluxServerRule()
 *
 *   @Test fun pingThatServer() {
 *     InfluxDBFactory.connect(serverRule.url).use { client ->
 *       client.ping() shouldBe pong
 *     }
 *   }
 * }
 * ```
 */
class InfluxServerRule internal constructor(builder: Builder) : ExternalResource() {

    val server: InfluxServer = builder.serverBuilder.build()

    val url: String
        get() = server.url

    constructor() : this(Builder())

    override fun before() {
        server.start()
    }

    override fun after() {
        server.close()
    }

    class Builder {
        internal var serverBuilder = InfluxServerBuilder()

        fun port(port: Int) = apply {
            serverBuilder = serverBuilder.port(port)
        }

        fun timeout(timeout: Duration) = apply {
            serverBuilder = serverBuilder.timeout(timeout)
        }

        fun build(): InfluxServerRule {
            return InfluxServerRule(this)
        }
    }
}
