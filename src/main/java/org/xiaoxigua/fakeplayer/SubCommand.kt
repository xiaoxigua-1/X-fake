package org.xiaoxigua.fakeplayer

import org.bukkit.command.CommandSender

abstract class SubCommand {
    abstract val name: String
    private val subCommands = mutableMapOf<String, SubCommand>()
    open fun onCommand(sender: CommandSender, args: MutableList<String>): Boolean {
        return true
    }

    open fun onTabComplete(sender: CommandSender, args: MutableList<String>): MutableList<String> {
        return mutableListOf()
    }

    fun addSunCommand(subCommand: SubCommand) {
        subCommands[subCommand.name] = subCommand
    }

    fun execute(sender: CommandSender, args: MutableList<String>): Boolean {
        return if (args.size == 1) {
            onCommand(sender, args)
        } else {
            val commandName = args.first()

            subCommands[commandName]?.execute(sender, args) ?: throw CommandError.CommandNotFound(commandName)
        }
    }

    fun tabComplete(sender: CommandSender, args: MutableList<String>): MutableList<String> {
        return if (args.size <= 1) {
            onTabComplete(sender, args).filter { Regex(args.first()).containsMatchIn(it) }.toMutableList()
        } else {
            subCommands[args.first()]?.tabComplete(sender, args) ?: mutableListOf()
        }
    }
}