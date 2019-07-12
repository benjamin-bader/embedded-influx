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

import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext

/**
 * [InfluxServerInstance] is a JUnit 5 extension which creates a new [InfluxServer] for each field using
 * the extension.
 *
 * This extension differs from [InfluxServerExtension] in that it will create a new [InfluxServer] for each
 * instance variable it is registered to.  Moreover, it allows the callers to configure the server by way
 * of the [InfluxServerBuilder].
 *
 * Example usage:
 *
 * ```
 * class ExampleTest {
 *     @JvmField
 *     @RegisterExtension
 *     val server = InfluxServerInstance(InfluxServerBuilder().timeout(Duration.ofSeconds(10)))
 *
 *     @Test fun pingThatServer() {
 *         InfluxDBFactory.connect(server.url).use { client ->
 *             val pong = client.ping()
 *             pong?.isGood shouldBe true
 *         }
 *     }
 * }
 * ```
 */
class InfluxServerInstance(
    private val builder: InfluxServerBuilder = InfluxServerBuilder()
) : BeforeEachCallback, AfterEachCallback {
    lateinit var server: InfluxServer

    val url: String
        get() = server.url

    override fun beforeEach(context: ExtensionContext) {
        server = builder.build()
        server.start()
    }

    override fun afterEach(context: ExtensionContext) {
        server.close()
    }
}