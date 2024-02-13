package org.xiaoxigua.fakeplayer

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter

class CommandManager(val commandName: String, val fakePlayers: MutableList<FakePlayerEntity>) {
    private val mainCommand = Bukkit.getPluginCommand(commandName)
    private val subCommands = mutableMapOf<String, SubCommand>()

    class MainCommandExecutor(private val commandManager: CommandManager) : CommandExecutor {
        override fun onCommand(
            sender: CommandSender,
            command: Command,
            commandString: String,
            commandARgs: Array<String>
        ): Boolean {
            val mutableListArgs = commandARgs.toMutableList()

            try {
                if (commandARgs.size < 2) {
                    sender.sendMessage(help())
                } else {
                    val args = mutableListOf(mutableListArgs.removeFirst())
                    val commandName = mutableListArgs.removeFirst()

                    commandManager.subCommands[commandName]?.execute(sender, mutableListArgs, args)
                        ?: throw CommandError.CommandNotFound(commandName)
                }

            } catch (commandError: Exception) {
                sender.sendMessage(Component.text(commandError.message ?: throw commandError, NamedTextColor.RED))
            }

            return true
        }

        private fun help(): Component {
            var helpText = Component.text("----------", NamedTextColor.YELLOW)
                .append(Component.text("Help", TextColor.color(0xffa500)))
                .append(Component.text("----------\n", NamedTextColor.YELLOW))
                .append(
                    Component.text(
                        "Usage: /$${commandManager.commandName} {fake player name} [subcommand]\n",
                        TextColor.color(0xffa500)
                    )
                )
                .append(Component.text("----------", NamedTextColor.YELLOW))
                .append(Component.text("Subcommands", TextColor.color(0xffa500)))
                .append(Component.text("----------\n", NamedTextColor.YELLOW))

            commandManager.subCommands.forEach { (_, command) ->
                helpText = helpText.append(Component.text(command.name, TextColor.color(0xffa500)))
                    .append(Component.text(": ${command.description}\n"))
            }

            return helpText
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
            val name = mutableListArgs.removeFirst()

            return if (args.size <= 1) {
                commandManager.fakePlayers.map { it.displayName }.plus(listOf("Alex", "Fake_Player"))
                    .filter { it.contains(name, ignoreCase = true) }.toMutableList()
            } else {
                val firstArg = mutableListArgs.removeFirst()

                commandManager.subCommands[firstArg]?.tabComplete(sender, mutableListArgs, mutableListOf(name))
                    ?: commandManager.subCommands.keys.filter { it.contains(firstArg, ignoreCase = true) }
                        .toMutableList()
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