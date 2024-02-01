package org.xiaoxigua.fakeplayer

import org.bukkit.command.CommandSender

abstract class SubCommand {
    abstract val name: String
    abstract val description: String
    open val permission = arrayOf<String>()
    private val subCommands = mutableMapOf<String, SubCommand>()
    abstract val fakePlayers: MutableList<FakePlayerEntity>

    open fun onCommand(sender: CommandSender, args: MutableList<String>): Boolean {
        return true
    }

    open fun onTabComplete(sender: CommandSender, args: MutableList<String>): MutableList<String> {
        return subCommands.keys.toMutableList()
    }

    fun addSunCommand(subCommandFun: (MutableList<FakePlayerEntity>) -> SubCommand) {
        val subCommand = subCommandFun(fakePlayers)

        subCommands[subCommand.name] = subCommand
    }

    fun execute(sender: CommandSender, args: MutableList<String>): Boolean {
        return if (args.size == 1) {
            onCommand(sender, args)
        } else {
            val commandName = args.removeAt(1)

            subCommands[commandName]?.execute(sender, args) ?: throw CommandError.CommandNotFound(commandName)
        }
    }

    fun tabComplete(sender: CommandSender, args: MutableList<String>): MutableList<String> {
        val firstArg = args.removeFirstOrNull() ?: ""

        return subCommands[firstArg]?.tabComplete(sender, args)
                ?: onTabComplete(sender, args).filter { Regex(firstArg).containsMatchIn(it) }.toMutableList()
    }
}