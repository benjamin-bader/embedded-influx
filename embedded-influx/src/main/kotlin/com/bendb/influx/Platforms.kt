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

internal enum class OperatingSystem(val value: String) {
    WINDOWS("win"),
    MACOS("mac"),
    LINUX("linux");

    val fileExtension: String
        get() = if (this == WINDOWS) ".exe" else ""
}

internal enum class Architecture(val value: String) {
    X86("x86"),
    X64("x64")
}

internal data class Platform(val os: OperatingSystem, val arch: Architecture)

internal fun detectPlatform(): Platform {
    val os = getOs()
    val arch = getArchitecture(os)
    return Platform(os, arch)
}

private fun getOs(): OperatingSystem {
    val osName = System.getProperty("os.name")

    return when {
        osName.contains("win") -> OperatingSystem.WINDOWS
        osName.equals("Mac OS X", ignoreCase = true) -> OperatingSystem.MACOS
        osName.contains(Regex("nix|nux|aix")) -> OperatingSystem.LINUX
        else -> error("Unsupported OS: $osName")
    }
}

private fun getArchitecture(os: OperatingSystem): Architecture {
    return when (os) {
        OperatingSystem.WINDOWS -> getWindowsArchitecture()
        OperatingSystem.MACOS -> getMacosArchitecture()
        OperatingSystem.LINUX -> getNixArchitecture()
    }
}

private fun getWindowsArchitecture(): Architecture {
    val processorArch = System.getenv("PROCESSOR_ARCHITECTURE")!!
    val wow64Arch = System.getenv("PROCESSOR_ARCHITEW6432")
    return if (processorArch.endsWith("64") || wow64Arch != null && wow64Arch.endsWith("64")) {
        Architecture.X64
    } else {
        Architecture.X86
    }
}

private fun getMacosArchitecture(): Architecture {
    return getOsArchitectureFromProcess("sysctl hw") { line ->
        line.contains("cpu64bit_capable") && line.endsWith("1")
    }
}

private fun getNixArchitecture(): Architecture {
    return getOsArchitectureFromProcess("uname -m") { line ->
        line.contains("64")
    }
}

private fun getOsArchitectureFromProcess(commandAndArguments: String, is64Bit: (String) -> Boolean): Architecture {
    try {
        val process = Runtime.getRuntime().exec(commandAndArguments)
        process.inputStream.bufferedReader().use { reader ->
            return if (reader.lineSequence().any(is64Bit)) {
                Architecture.X64
            } else {
                Architecture.X86
            }
        }
    } catch (e: Exception) {
        error("Failed to detect architecture with command '$commandAndArguments': $e")
    }
}