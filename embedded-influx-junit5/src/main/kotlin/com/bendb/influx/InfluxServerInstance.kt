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
 *         val pong = client.ping()
 *         pong?.isGood shouldBe true
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