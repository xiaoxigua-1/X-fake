package org.xiaoxigua.fakeplayer.commands.attack

import org.bukkit.command.CommandSender
import org.bukkit.scheduler.BukkitRunnable
import org.xiaoxigua.fakeplayer.FakePlayerEntity
import org.xiaoxigua.fakeplayer.FakePlayerPlugin
import org.xiaoxigua.fakeplayer.FakePlayerTask
import org.xiaoxigua.fakeplayer.SubCommand

class Interval(override val fakePlayers: MutableList<FakePlayerEntity>) : SubCommand() {
    override val name = "interval"
    override val description = "set periodic attack"

    private val defaultTime = 20L

    override fun onCommand(sender: CommandSender, args: MutableList<String>): Boolean {
        val name = args.removeFirst()
        val fakePlayer = fakePlayers.find { it.displayName == name }

        if (args.isEmpty() && fakePlayer != null) {
            val task = object : BukkitRunnable() {
                override fun run() {
                    fakePlayer.attack()
                }
            }.runTaskTimer(FakePlayerPlugin.currentPlugin!!, 0L, defaultTime)

            fakePlayer.taskManager.addTask(FakePlayerTask.TaskType.Attack, task)
        }

        return super.onCommand(sender, args)
    }
}