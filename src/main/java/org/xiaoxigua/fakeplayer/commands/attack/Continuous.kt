package org.xiaoxigua.fakeplayer.commands.attack

import org.bukkit.command.CommandSender
import org.bukkit.scheduler.BukkitRunnable
import org.xiaoxigua.fakeplayer.*

class Continuous(override val fakePlayers: MutableList<FakePlayerEntity>) : SubCommand() {
    override val name = "interval"
    override val description = "set periodic attack"

    private val defaultTime = 20

    override fun onCommand(
        sender: CommandSender,
        commandArgs: MutableList<String>,
        args: MutableList<String>
    ): Boolean {
        val name = args.first()
        val fakePlayer = fakePlayers.find { it.displayName == name }
            ?: throw CommandError.CommandFakePlayerNotFound(name)
        val time = (commandArgs.removeFirstOrNull() ?: defaultTime.toString()).takeIf {
            Regex("\\d+").matches(it)
        }?.toLong() ?: throw CommandError.CommandArgTypeError("Int")
        val task = object : BukkitRunnable() {
            override fun run() {
                fakePlayer.attack()
            }
        }.runTaskTimer(FakePlayerPlugin.currentPlugin!!, 0L, time)

        fakePlayer.taskManager.addTask(FakePlayerTask.TaskType.Attack, task)

        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        commandArgs: MutableList<String>,
        args: MutableList<String>
    ): MutableList<String> {
        return if (commandArgs.size < 1) {
            (10..50 step 10).map(Int::toString)
        } else {
            listOf()
        }.toMutableList()
    }
}