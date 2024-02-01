package org.xiaoxigua.fakeplayer

import org.bukkit.command.CommandSender

abstract class SubCommand {
    abstract val name: String
    abstract val description: String
    open val permission = arrayOf<String>()
    private val subCommands = mutableMapOf<String, SubCommand>()
    abstract val fakePlayers: MutableList<FakePlayerEntity>

    open fun onCommand(sender: CommandSender, commandArgs: MutableList<String>, args: MutableList<String>): Boolean {
        return true
    }

    open fun onTabComplete(sender: CommandSender, commandArgs: MutableList<String>): MutableList<String> {
        return subCommands.keys.toMutableList()
    }

    fun addSunCommand(subCommandFun: (MutableList<FakePlayerEntity>) -> SubCommand) {
        val subCommand = subCommandFun(fakePlayers)

        subCommands[subCommand.name] = subCommand
    }

    fun execute(sender: CommandSender, commandArgs: MutableList<String>, args: MutableList<String>): Boolean {
        val commandName = commandArgs.firstOrNull()
        val subCommand = subCommands[commandName]

        return if (subCommand != null) {
            commandArgs.removeFirst()
            subCommand.execute(sender, commandArgs, args)
        } else {
            onCommand(sender, commandArgs, args)
        }
    }

    fun tabComplete(sender: CommandSender, args: MutableList<String>): MutableList<String>? {
        val firstArg = args.removeFirstOrNull()

        return if (firstArg != null)
            subCommands[firstArg]?.tabComplete(sender, args)
                    ?: onTabComplete(sender, args).filter { it.contains(firstArg, ignoreCase = true) }.toMutableList()
        else null
    }
}