package org.xiaoxigua.fakeplayer

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter

class CommandManager(commandName: String, val fakePlayers: MutableList<FakePlayerEntity>) {
    private val mainCommand = Bukkit.getPluginCommand(commandName)
    private val subCommands = mutableMapOf<String, SubCommand>()

    class MainCommandExecutor(private val commandManager: CommandManager) : CommandExecutor {
        override fun onCommand(sender: CommandSender, command: Command, commandString: String, args: Array<String>): Boolean {
            val mutableListArgs = args.toMutableList()

            try {
                if (args.size < 2) {
                    // Help
                } else {
                    val commandName = mutableListArgs.removeAt(1)
                    commandManager.subCommands[commandName]?.execute(sender, mutableListArgs)
                            ?: throw CommandError.CommandNotFound(commandName)
                }

            } catch (commandError: Exception) {
                sender.sendMessage(Component.text(commandError.message ?: throw commandError, NamedTextColor.RED))
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
            mutableListArgs.removeFirst()

            return if (args.size <= 1) {
                commandManager.fakePlayers.map { it.displayName }.plus(listOf("Alex", "Fake_Player")).toMutableList()
            } else {
                val firstArg = mutableListArgs.removeFirst()

                commandManager.subCommands[firstArg]?.tabComplete(sender, mutableListArgs)
                        ?: commandManager.subCommands.keys.filter { it.contains(firstArg, ignoreCase = true) }.toMutableList()
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

    fun addSubCommand(vararg subCommands: (MutableList<FakePlayerEntity>) -> SubCommand) {
        subCommands.forEach {
            addSubCommand(it(fakePlayers))
        }
    }
}