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
import java.io.IOException
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes

internal object ShutdownHooks {
    /**
     * Like File#deleteOnExit(), but better.  Deletes the given directory
     * and its contents when the VM shuts down.
     *
     * The return value of this method is a [Thread], which is the actual
     * hook object.  If so desired, it can be manually run ahead-of-time,
     * or could be unregistered from the runtime's list of shutdown hooks.
     *
     * @param directory the directory to be deleted
     * @return the hook object itself.
     */
    internal fun deleteRecursively(directory: File): Thread {
        return deleteRecursively(directory.toPath())
    }

    /**
     * Like File#deleteOnExit(), but better.  Deletes the given directory
     * and its contents when the VM shuts down.
     *
     * The return value of this method is a [Thread], which is the actual
     * hook object.  If so desired, it can be manually run ahead-of-time,
     * or could be unregistered from the runtime's list of shutdown hooks.
     *
     * @param path the directory to be deleted
     * @return the hook object itself.
     */
    internal fun deleteRecursively(path: Path): Thread {
        val hook = Thread {
            try {
                Files.walkFileTree(path, DeleteAllTheThings)
            } catch (e: IOException) {
                // whoops!
            }

            Files.deleteIfExists(path)
        }

        Runtime.getRuntime().addShutdownHook(hook)

        return hook
    }
}

private object DeleteAllTheThings : SimpleFileVisitor<Path>() {
    override fun visitFile(file: Path?, attrs: BasicFileAttributes?): FileVisitResult {
        if (file != null) {
            Files.deleteIfExists(file)
        }
        return FileVisitResult.CONTINUE
    }

    override fun postVisitDirectory(dir: Path?, exc: IOException?): FileVisitResult {
        if (dir != null && exc == null) {
            Files.deleteIfExists(dir)
        }
        return FileVisitResult.CONTINUE
    }
}
