package com.bendb.influx

import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.Extension
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.TestInstancePostProcessor

class InfluxServerExtension(
    private val port: Int = 8086
) : Extension, TestInstancePostProcessor, BeforeEachCallback, AfterEachCallback {

    private val extensionNamespace = ExtensionContext.Namespace.create(javaClass)

    private val ExtensionContext.influxStore: ExtensionContext.Store
        get() = getStore(extensionNamespace)

    // region TestInstancePostProcessor

    override fun postProcessTestInstance(testInstance: Any, context: ExtensionContext) {
        for (field in testInstance.javaClass.declaredFields) {
            field.isAccessible = true
            if (field.type.isAssignableFrom(InfluxServer::class.java)) {
                val store = context.influxStore
                val server = store.getOrComputeIfAbsent("server") { InfluxServer(port) }

                field.set(testInstance, server)
            }
        }
    }

    // endregion

    // region BeforeEachCallback

    override fun beforeEach(context: ExtensionContext) {
        val server = context.influxStore.get("server") as? InfluxServer
        server?.start()
    }

    // endregion

    // region AfterEachCallback

    override fun afterEach(context: ExtensionContext) {
        val server = context.influxStore.get("server") as? InfluxServer
        server?.close()
    }

    // endregion
}