package org.xiaoxigua.fakeplayer.commands

import org.bukkit.command.CommandSender
import org.xiaoxigua.fakeplayer.*

class Stop(override val fakePlayers: MutableList<FakePlayerEntity>) : SubCommand() {
    override val name = "stop"
    override val description = "stop fake player all task"


    override fun onCommand(
        sender: CommandSender,
        commandArgs: MutableList<String>,
        args: MutableList<String>
    ): Boolean {
        val name = args.first()
        val fakePlayer =
            fakePlayers.find { it.displayName == name } ?: throw CommandError.CommandFakePlayerNotFound(name)

        fakePlayer.taskManager.removeAllTask()

        return super.onCommand(sender, commandArgs, args)
    }
}