package com.bendb.influx

import org.junit.rules.ExternalResource

/**
 * A JUnit 4 `@Rule` that creates and starts an [InfluxServer] before and
 * after each test in the class, respectively.
 */
class InfluxServerRule internal constructor(builder: Builder) : ExternalResource() {

    val server: InfluxServer = builder.serverBuilder.build()

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