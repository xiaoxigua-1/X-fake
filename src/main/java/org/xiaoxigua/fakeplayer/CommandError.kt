package org.xiaoxigua.fakeplayer

sealed class CommandError(override val message: String) : Exception() {
    data class CommandNotFound(val commandName: String) : CommandError("Command $commandName not found")

    data class CommandStringLimit(val max: Int, val current: Int) : CommandError("String to big (was $current characters, max $max)")
}