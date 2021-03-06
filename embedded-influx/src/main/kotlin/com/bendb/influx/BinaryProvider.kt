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

import java.io.File
import java.io.InputStream
import java.util.function.Supplier

internal object BinaryProvider : Supplier<File> {
    override fun get(): File {
        val platform = detectPlatform()
        if (!platform.isSupported) {
            error("No Influx binary found for the current platform: $platform")
        }

        return copyExecutableResource(platform.binaryName)
    }
}

internal val Platform.isSupported: Boolean
    get() {
        return arch == Architecture.X64
    }

internal val Platform.binaryName: String
    get() = pathFromComponents(os.value, arch.value, "influxd${os.fileExtension}")

internal fun copyExecutableResource(filename: String): File {
    val tempDir = createTempDir(prefix = "embedded_influx").also { ShutdownHooks.deleteRecursively(it) }

    val exe = File(tempDir, filename).apply {
        parentFile.mkdirs()
    }

    loadResourceAsStream(filename).use { input ->
        exe.outputStream().use { output ->
            input.copyTo(output)
            output.flush()
        }
    }

    exe.setExecutable(true)

    return exe
}

private fun loadResourceAsStream(resourceName: String): InputStream {
    val classLoader = Thread.currentThread().contextClassLoader
        ?: BinaryProvider.javaClass.classLoader
        ?: error("Both Thread.getContextClassLoader() and BinaryProvider.class.getClassLoader() are null")

    return classLoader.getResourceAsStream(resourceName) ?: error(
        "Failed to load expected JAR resource '$resourceName'"
    )
}

private fun pathFromComponents(vararg components: String): String {
    return components.joinToString("/")
}