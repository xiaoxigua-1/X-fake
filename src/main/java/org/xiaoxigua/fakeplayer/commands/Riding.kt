package org.xiaoxigua.fakeplayer.commands

import org.bukkit.command.CommandSender
import org.xiaoxigua.fakeplayer.CommandError
import org.xiaoxigua.fakeplayer.FakePlayerEntity
import org.xiaoxigua.fakeplayer.SubCommand

class Riding(override val fakePlayers: MutableList<FakePlayerEntity>) : SubCommand() {
    override val name = "riding"
    override val description = "fake player riding entity"

    override fun onCommand(
        sender: CommandSender,
        commandArgs: MutableList<String>,
        args: MutableList<String>
    ): Boolean {
        val name = args.removeFirst()

        fakePlayers.find { it.displayName == name }?.riding() ?: throw CommandError.CommandFakePlayerNotFound(name)

        return true
    }
}