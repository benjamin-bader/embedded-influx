package com.bendb.influx

import org.junit.rules.ExternalResource

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

    val url: String = server.url

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

        fun build(): InfluxServerRule {
            return InfluxServerRule(this)
        }
    }
}
