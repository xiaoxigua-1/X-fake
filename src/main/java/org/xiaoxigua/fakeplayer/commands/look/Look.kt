package org.xiaoxigua.fakeplayer.commands.look

import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.xiaoxigua.fakeplayer.CommandError
import org.xiaoxigua.fakeplayer.FakePlayerEntity
import org.xiaoxigua.fakeplayer.SubCommand

class Look(override val fakePlayers: MutableList<FakePlayerEntity>) : SubCommand() {
    override val name = "look"
    override val description = "set The player looks in the current"

    override fun onCommand(
        sender: CommandSender,
        commandArgs: MutableList<String>,
        args: MutableList<String>
    ): Boolean {
        val name = args.first()
        val fakePlayer =
            fakePlayers.find { it.displayName == name } ?: throw CommandError.CommandFakePlayerNotFound(name)

        if (sender is Player)
            fakePlayer.setRot(sender.location.yaw, sender.location.pitch)

        return true
    }

    init {
        addSubCommand(::Linear)
    }
}