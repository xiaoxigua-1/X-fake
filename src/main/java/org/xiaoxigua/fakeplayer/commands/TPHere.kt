package org.xiaoxigua.fakeplayer.commands

import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.xiaoxigua.fakeplayer.CommandError
import org.xiaoxigua.fakeplayer.FakePlayerEntity
import org.xiaoxigua.fakeplayer.SubCommand

class TPHere(override val fakePlayers: MutableList<FakePlayerEntity>) : SubCommand() {
    override val name = "tphere"
    override val description = "tp fake player to your position"

    override fun onCommand(
        sender: CommandSender,
        commandArgs: MutableList<String>,
        args: MutableList<String>
    ): Boolean {
        val name = args.first()

        if (sender is Player) {
            fakePlayers.find { it.displayName == name }
                ?.bukkitEntity?.teleport(sender)
                ?: throw CommandError.CommandFakePlayerNotFound(name)
        }

        return true
    }
}