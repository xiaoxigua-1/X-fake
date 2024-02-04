package org.xiaoxigua.fakeplayer

sealed class CommandError(override val message: String) : Exception() {
    data class CommandNotFound(val commandName: String) : CommandError("Command $commandName not found")

    data class CommandStringLimit(val max: Int, val current: Int) : CommandError("String to big (was $current characters, max $max)")

    data class CommandFakePlayerNotFound(val name: String) : CommandError("$name fake player not found")

    data class CommandArgTypeError(val expected: String) : CommandError("Expected arg type $expected")

    data class CommandMissingArg(val argName: String) : CommandError("Missing $argName")

    data class CommandItemNotFound(val itemName: String) : CommandError("Item $itemName not found")

    data class CommandItemCantCraft(val item: String) : CommandError("Item $item cant craft")
}