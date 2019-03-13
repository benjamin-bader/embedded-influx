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

import java.io.Closeable
import java.io.File
import java.net.ServerSocket
import java.nio.file.Files
import java.time.Duration
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock
import java.util.function.Supplier
import kotlin.concurrent.thread
import kotlin.concurrent.withLock

/**
 * An object that can host an instance of InfluxDB.
 *
 * Without any custom configuration, [InfluxServer] will start InfluxDB with
 * the default HTTP port of 8086, using the database's default configuration.
 *
 * At present, an affordance is made in the API for customizing the listening
 * port, but it has no effect.  This situation is expected to change soon.
 */
class InfluxServer internal constructor(builder: InfluxServerBuilder): Closeable {

    companion object {
        /**
         * Creates and returns an [InfluxServerBuilder].
         */
        @JvmStatic fun builder(): InfluxServerBuilder = InfluxServerBuilder()
    }

    private val exe = builder.executableSupplier.get()
    private val timeout = builder.timeout

    private var backupPort: Int = 0
    private var httpPort: Int = builder.port

    private val lock = ReentrantLock()
    private val serverActiveCondition = lock.newCondition()
    private var process: Process? = null
    private var started = false

    /**
     * The URL at which the server will listen for HTTP queries.
     */
    val url: String
        get() = "http://127.0.0.1:$httpPort"

    /**
     * Creates a new instance of [InfluxServer], which when started will listen
     * for HTTP queries on the given [port].
     */
    constructor(port: Int) : this(builder().port(port))

    /**
     * Creates a new instance of [InfluxServer], using any available port.
     */
    constructor() : this(builder())

    /**
     * Starts the Influx server instance, and waits for it to become
     * responsive.
     */
    fun start() {
        lock.withLock {
            if (process != null) {
                return
            }

            backupPort = if (backupPort == 0) findRandomPort() else backupPort
            httpPort = if (httpPort == 0) findRandomPort() else httpPort

            val rootDirectory = Files.createTempDirectory("influx").toFile()
            val configFile = File.createTempFile("influx", ".conf", rootDirectory)
            val metaDir = File(rootDirectory, "meta").also { it.mkdirs() }
            val dataDir = File(rootDirectory, "data").also { it.mkdirs() }
            val walDir = File(rootDirectory, "wal").also { it.mkdirs() }

            // File#deleteOnExit doesn't handle non-empty dirs, and influx
            // writes files that we can't cleanly incorporate into deleteOnExit;
            // we'll just use our own version instead.
            ShutdownHooks.deleteRecursively(rootDirectory)

            configFile.writeText("""
                reporting-disabled = true
                bind-address = "127.0.0.1:$backupPort"

                [meta]
                dir = "${metaDir.escaped}"

                [data]
                dir = "${dataDir.escaped}"
                wal-dir = "${walDir.escaped}"

                [http]
                bind-address = ":$httpPort"
            """.trimIndent())

            val proc = ProcessBuilder(listOf(exe.absolutePath, "-config", configFile.absolutePath))
                .directory(exe.parentFile)
                .start() ?: error("Assertion failed - ProcessBuilder.start() returned null")

            process = proc

            thread { awaitServerActive(proc) }

            val deadline = System.currentTimeMillis() + timeout.toMillis()

            while (true) {
                val finished = lock.withLock { started }
                if (finished) {
                    break
                }
                val toWait = deadline - System.currentTimeMillis()
                if (toWait < 0) {
                    proc.destroy()
                    process = null
                    error("Timed out waiting for InfluxDB to start")
                }

                serverActiveCondition.await(toWait, TimeUnit.MILLISECONDS)
            }
        }
    }

    private fun awaitServerActive(process: Process) {
        process.errorStream.bufferedReader().also { reader ->
            while (true) {
                val line = reader.readLine() ?: return

                println(line)

                if (line.contains("Listening for signals")) {
                    lock.withLock {
                        started = true
                        serverActiveCondition.signal()
                    }
                    return
                }
            }
        }
    }

    /**
     * Shuts down the server process, if it is active.
     */
    override fun close() {
        val process = lock.withLock {
            started = false
            val proc = process
            process = null
            proc
        } ?: return

        process.destroy()
        try {
            process.waitFor()
        } catch (e: InterruptedException) {
            error("Failed to stop InfluxDB")
        }
    }

    @Suppress("ProtectedInFinal", "unused")
    protected fun finalize() {
        try {
            process?.destroyForcibly()
        } catch (ignored: Exception) {
            // noop
        }
    }

    private fun findRandomPort(): Int {
        // ServerSockets given a port of zero will automatically
        // find and bind to an unallocated port.  We can close
        // the server socket and reuse its port, since we can be
        // fairly confident that nothing else will take the port
        // between here and starting up influx.
        return ServerSocket(0).use {
            it.localPort
        }
    }

    private val File.escaped: String
        get() = absolutePath.escaped

    private val String.escaped: String
        get() = replace("\\", "\\\\")
}

class InfluxServerBuilder {

    internal var port: Int = 0
    internal var executableSupplier: Supplier<File> = BinaryProvider
    internal var timeout: Duration = Duration.ofSeconds(2)

    fun port(port: Int) = apply { this.port = port}
    fun executableSupplier(supplier: Supplier<File>) = apply { this.executableSupplier = supplier }
    fun timeout(timeout: Duration) = apply { this.timeout = timeout }

    fun build(): InfluxServer {
        return InfluxServer(this)
    }
}