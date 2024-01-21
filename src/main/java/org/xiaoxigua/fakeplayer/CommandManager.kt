package org.xiaoxigua.fakeplayer

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
            val args = args.toMutableList()

            if (args.isEmpty()) {
                // Help
            } else {
                val last = args.removeLast()

                commandManager.subCommands[last]?.onCommand(sender, args)
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
            return commandManager.subCommands.keys.toMutableList()
        }
    }

    init {
        mainCommand?.setExecutor(MainCommandExecutor(this))
        mainCommand?.tabCompleter = MainCommandTabCompleter(this)
    }

    fun addSubCommand(subCommand: SubCommand) {
        subCommands[subCommand.name] = subCommand
    }
}