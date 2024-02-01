package org.xiaoxigua.fakeplayer.commands

import org.bukkit.command.CommandSender
import org.xiaoxigua.fakeplayer.CommandError
import org.xiaoxigua.fakeplayer.FakePlayerEntity
import org.xiaoxigua.fakeplayer.SubCommand

class Kill(override val fakePlayers: MutableList<FakePlayerEntity>) : SubCommand() {
    override val name = "kill"
    override val description = "kill fake player"

    override fun onCommand(sender: CommandSender, commandArgs: MutableList<String>, args: MutableList<String>): Boolean {
        val name = args.removeFirst()

        fakePlayers.find { it.displayName == name }?.kill() ?: throw CommandError.CommandFakePlayerNotFound(name)

        return true
    }
}