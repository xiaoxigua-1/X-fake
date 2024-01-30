package org.xiaoxigua.fakeplayer

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter

class CommandManager(commandName: String) {
    private val mainCommand = Bukkit.getPluginCommand(commandName)
    private val subCommands = mutableMapOf<String, SubCommand>()

    class MainCommandExecutor(private val commandManager: CommandManager) : CommandExecutor {
        override fun onCommand(sender: CommandSender, command: Command, commandString: String, args: Array<String>): Boolean {
            val mutableListArgs = args.toMutableList()

            if (args.isEmpty()) {
                // Help
            } else {
                val commandName = mutableListArgs.removeFirst()

                try {
                    commandManager.subCommands[commandName]?.execute(sender, mutableListArgs) ?: throw CommandError.CommandNotFound(commandName)
                } catch (commandError: Exception) {
                    sender.sendMessage(Component.text(commandError.message ?: throw commandError, NamedTextColor.RED))
                }
            }

            return true
        }
    }


    class MainCommandTabCompleter(private val commandManager: CommandManager) : TabCompleter {
        override fun onTabComplete(
            sender: CommandSender,
            command: Command,
            string: String,
            args: Array<String>
        ): MutableList<String> {
            val mutableListArgs = args.toMutableList()

            return if (args.size <= 1) {
                commandManager.subCommands.keys.filter { Regex(args.first()).containsMatchIn(it) }.toMutableList()
            } else {
                commandManager.subCommands[mutableListArgs.removeFirst()]?.tabComplete(sender, mutableListArgs) ?: mutableListOf()
            }
        }
    }

    init {
        mainCommand?.setExecutor(MainCommandExecutor(this))
        mainCommand?.tabCompleter = MainCommandTabCompleter(this)
    }

    private fun addSubCommand(subCommand: SubCommand) {
        subCommands[subCommand.name] = subCommand
    }

    fun addSubCommand(vararg subCommands: SubCommand) {
        subCommands.forEach {
            addSubCommand(it)
        }
    }
}