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

import io.kotlintest.matchers.file.exist
import io.kotlintest.should
import io.kotlintest.shouldNot
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class ShutdownHooksTest {
    @get:Rule val tempDir = TemporaryFolder()

    @Test
    fun `deleteRecursively deletes non-empty directories`() {
        val dir = tempDir.newFolder()
        val file = File(dir, "test.txt")
        file.writeText("foo")

        val hook = ShutdownHooks.deleteRecursively(dir)
        Runtime.getRuntime().removeShutdownHook(hook) // We are running the hook ourselves here

        dir should exist()
        file should exist()

        hook.run()

        dir shouldNot exist()
        file shouldNot exist()
    }

    @Test
    fun `deleteRecursively does not crash if the directory does not exist`() {
        val dir = File("foo")
        val hook = ShutdownHooks.deleteRecursively(dir)
        Runtime.getRuntime().removeShutdownHook(hook)

        hook.run()
    }

    @Test
    fun `deleteRecursively handles files too`() {
        val file = tempDir.newFile()
        file.writeText("blah")

        val hook = ShutdownHooks.deleteRecursively(file)
        Runtime.getRuntime().removeShutdownHook(hook) // We are running the hook ourselves here

        file should exist()
        hook.run()
        file shouldNot exist()
    }
}