package org.xiaoxigua.fakeplayer.commands.breaking

import org.bukkit.command.CommandSender
import org.xiaoxigua.fakeplayer.CommandError
import org.xiaoxigua.fakeplayer.FakePlayerEntity
import org.xiaoxigua.fakeplayer.FakePlayerTask
import org.xiaoxigua.fakeplayer.SubCommand

class BreakBlock(override val fakePlayers: MutableList<FakePlayerEntity>) : SubCommand() {
    override val name = "break"
    override val description = "set fake player breaking action"

    init {
        addSubCommand(::Continuous)
    }

    override fun onCommand(
        sender: CommandSender,
        commandArgs: MutableList<String>,
        args: MutableList<String>
    ): Boolean {
        val name = args.first()
        val fakePlayer =
            fakePlayers.find { it.displayName == name } ?: throw CommandError.CommandFakePlayerNotFound(name)

        fakePlayer.taskManager.addTask(FakePlayerTask.TaskType.Breaking, fakePlayer.breaking())
        return true
    }
}