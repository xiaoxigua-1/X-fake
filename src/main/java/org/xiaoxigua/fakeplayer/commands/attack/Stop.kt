package org.xiaoxigua.fakeplayer.commands.attack

import org.bukkit.command.CommandSender
import org.xiaoxigua.fakeplayer.CommandError
import org.xiaoxigua.fakeplayer.FakePlayerEntity
import org.xiaoxigua.fakeplayer.FakePlayerTask
import org.xiaoxigua.fakeplayer.SubCommand

class Stop(override val fakePlayers: MutableList<FakePlayerEntity>) : SubCommand() {
    override val name = "stop"
    override val description = "stop all attack task"

    override fun onCommand(sender: CommandSender, commandArgs: MutableList<String>, args: MutableList<String>): Boolean {
        val name = args.first()
        val fakePlayer = fakePlayers.find { it.displayName == name }
                ?: throw CommandError.CommandFakePlayerNotFound(name)

        fakePlayer.taskManager.removeTask(FakePlayerTask.TaskType.Attack)

        return true
    }
}