package org.xiaoxigua.fakeplayer.commands.attack

import org.bukkit.command.CommandSender
import org.xiaoxigua.fakeplayer.CommandError
import org.xiaoxigua.fakeplayer.FakePlayerEntity
import org.xiaoxigua.fakeplayer.SubCommand

class Attack(override val fakePlayers: MutableList<FakePlayerEntity>) : SubCommand() {
    override val name = "attack"
    override val description = "set fake player attack action"

    init {
        addSunCommand(::Interval)
    }

    override fun onCommand(sender: CommandSender, commandArgs: MutableList<String>, args: MutableList<String>): Boolean {
        fakePlayers.find { it.displayName == args.first() }?.attack()
                ?: throw CommandError.CommandFakePlayerNotFound(args.first())

        return true
    }
}