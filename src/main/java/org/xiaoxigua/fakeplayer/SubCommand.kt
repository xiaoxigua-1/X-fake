package org.xiaoxigua.fakeplayer

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
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

    fun addSubCommand(subCommandFun: (MutableList<FakePlayerEntity>) -> SubCommand) {
        val subCommand = subCommandFun(fakePlayers)

        subCommands[subCommand.name] = subCommand
    }

    fun addSubCommand(vararg subCommand: (MutableList<FakePlayerEntity>) -> SubCommand) {
        subCommand.forEach {
            addSubCommand(it)
        }
    }

    fun execute(sender: CommandSender, commandArgs: MutableList<String>, args: MutableList<String>): Boolean {
        val commandName = commandArgs.firstOrNull()
        val subCommand = subCommands[commandName]

        return if (commandName == "help") {
            sender.sendMessage(help())
            true
        } else if (subCommand != null) {
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
                ?: onTabComplete(sender, args).filter { it.contains(args.lastOrNull() ?: firstArg, ignoreCase = true) }
                    .toMutableList()
        else null
    }

    private fun help(): Component {
        var helpText = Component.text("----------", NamedTextColor.YELLOW)
            .append(Component.text("Help", TextColor.color(0xffa500)))
            .append(Component.text("----------\n", NamedTextColor.YELLOW))
            .append(Component.text("$name command subcommands\n", NamedTextColor.DARK_RED))

        subCommands.forEach { (_, command) ->
            helpText = helpText.append(Component.text(command.name, TextColor.color(0xffa500)))
                .append(Component.text(": ${command.description}\n"))
        }

        return helpText
    }
}