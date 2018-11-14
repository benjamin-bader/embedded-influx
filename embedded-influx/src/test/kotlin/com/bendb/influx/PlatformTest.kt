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

import io.kotlintest.fail
import io.kotlintest.matchers.endWith
import io.kotlintest.matchers.startWith
import io.kotlintest.should
import org.junit.Test

class PlatformTest {
    @Test fun windowsBinaryNameEndsWithExe() {
        Platform(OperatingSystem.WINDOWS, Architecture.X64).binaryName should endWith("influxd.exe")
    }

    @Test fun windowsBinaryNameStartsWithWin() {
        Platform(OperatingSystem.WINDOWS, Architecture.X64).binaryName should startWith("win")
    }

    @Test fun macosBinaryNameDoesNotHaveFileExtension() {
        Platform(OperatingSystem.MACOS, Architecture.X64).binaryName should endWith("influxd")
    }

    @Test fun macosBinaryNameStartsWithMac() {
        Platform(OperatingSystem.MACOS, Architecture.X64).binaryName should startWith("mac")
    }

    @Test fun linuxBinaryNameDoesNotHaveFileExtension() {
        Platform(OperatingSystem.LINUX, Architecture.X64).binaryName should endWith("influxd")
    }

    @Test fun linuxBinaryNameStartsWithLinux() {
        Platform(OperatingSystem.LINUX, Architecture.X64).binaryName should startWith("linux")
    }

    @Test fun `influx binaries are bundled for all supported platforms`() {
        val oss = OperatingSystem.values()
        val archs = Architecture.values()
        val platforms = oss.cartesianProduct(archs) { os, arch -> Platform(os, arch) }

        for (platform in platforms) {
            if (!platform.isSupported) continue

            val resourceUrl = javaClass.classLoader.getResource(platform.binaryName)
            if (resourceUrl == null) {
                fail("Missing resource named ${platform.binaryName} for supported platform $platform")
            }
        }
    }
}

inline fun <A, B, R> Array<A>.cartesianProduct(other: Array<B>, fn: (A, B) -> R): Iterable<R> {
    return flatMap { a -> other.map { b -> fn(a, b) } }
}