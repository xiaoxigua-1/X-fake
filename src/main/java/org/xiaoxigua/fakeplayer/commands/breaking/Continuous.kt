package org.xiaoxigua.fakeplayer.commands.breaking

import org.bukkit.command.CommandSender
import org.bukkit.scheduler.BukkitRunnable
import org.xiaoxigua.fakeplayer.*

class Continuous(override val fakePlayers: MutableList<FakePlayerEntity>) : SubCommand() {
    override val name = "continuous"
    override val description = "set continuous break block"

    override fun onCommand(
        sender: CommandSender,
        commandArgs: MutableList<String>,
        args: MutableList<String>
    ): Boolean {
        val name = args.first()
        val fakePlayer =
            fakePlayers.find { it.displayName == name } ?: throw CommandError.CommandFakePlayerNotFound(name)

        val task = object : BukkitRunnable() {
            override fun run() {
                if (fakePlayer.taskManager.getTask(FakePlayerTask.TaskType.Breaking)?.isCancelled == true)
                    fakePlayer.taskManager.addTask(FakePlayerTask.TaskType.Breaking, fakePlayer.breaking())
            }
        }.runTaskTimer(FakePlayerPlugin.currentPlugin!!, 0L, 2L)

        fakePlayer.taskManager.addTask(FakePlayerTask.TaskType.ContinuousBreaking, task)

        return true
    }
}