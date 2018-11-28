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
import org.junit.jupiter.api.extension.Extension
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.TestInstancePostProcessor

class InfluxServerExtension :
    Extension, TestInstancePostProcessor, BeforeEachCallback, AfterEachCallback {

    private val extensionNamespace = ExtensionContext.Namespace.create(javaClass)

    private val ExtensionContext.influxStore: ExtensionContext.Store
        get() = getStore(extensionNamespace)

    // region TestInstancePostProcessor

    override fun postProcessTestInstance(testInstance: Any, context: ExtensionContext) {
        for (field in testInstance.javaClass.declaredFields) {
            field.isAccessible = true
            if (field.type.isAssignableFrom(InfluxServer::class.java)) {
                val store = context.influxStore
                val server = store.getOrComputeIfAbsent("server") { InfluxServer() }

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