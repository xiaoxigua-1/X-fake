package org.xiaoxigua.fakeplayer.commands.attack

import org.bukkit.command.CommandSender
import org.xiaoxigua.fakeplayer.FakePlayerEntity
import org.xiaoxigua.fakeplayer.SubCommand

class Attack(private val fakePlayers: MutableList<FakePlayerEntity>) : SubCommand() {
    override val name = "attack"

    override fun onCommand(sender: CommandSender, args: MutableList<String>): Boolean {
        val name = args.removeFirst()

        fakePlayers.find { it.displayName == name }?.attack()

        return true
    }
}