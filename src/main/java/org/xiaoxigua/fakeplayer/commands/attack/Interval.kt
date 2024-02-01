package org.xiaoxigua.fakeplayer.commands.attack

import org.bukkit.command.CommandSender
import org.bukkit.scheduler.BukkitRunnable
import org.xiaoxigua.fakeplayer.*

class Interval(override val fakePlayers: MutableList<FakePlayerEntity>) : SubCommand() {
    override val name = "interval"
    override val description = "set periodic attack"

    private val defaultTime = 20L

    override fun onCommand(sender: CommandSender, args: MutableList<String>): Boolean {
        val name = args.removeFirst()
        val fakePlayer = fakePlayers.find { it.displayName == name }
                ?: throw CommandError.CommandFakePlayerNotFound(name)
        val time = args.removeFirstOrNull()?.takeIf {
            Regex("\\d+").matches(it)
        }?.toLong() ?: defaultTime

        if (args.isEmpty()) {
            val task = object : BukkitRunnable() {
                override fun run() {
                    fakePlayer.attack()
                }
            }.runTaskTimer(FakePlayerPlugin.currentPlugin!!, 0L, time)

            fakePlayer.taskManager.addTask(FakePlayerTask.TaskType.Attack, task)
        }

        return super.onCommand(sender, args)
    }

    override fun onTabComplete(sender: CommandSender, args: MutableList<String>): MutableList<String> {
        return (10..50 step 10).map(Int::toString).toMutableList()
    }
}