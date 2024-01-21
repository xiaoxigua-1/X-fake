package org.xiaoxigua.fakeplayer.commands

import org.bukkit.command.CommandSender
import org.xiaoxigua.fakeplayer.FakePlayerEntity
import org.xiaoxigua.fakeplayer.SubCommand

class Kill(private val fakePlayers: MutableList<FakePlayerEntity>) : SubCommand() {
    override val name = "kill"

    override fun onCommand(sender: CommandSender, args: MutableList<String>): Boolean {
        val name = args.removeFirst()

        fakePlayers.find { it.displayName == name }?.kill()

        return true
    }

    override fun onTabComplete(sender: CommandSender, args: MutableList<String>): MutableList<String> {
        return fakePlayers.map { it.displayName }.toMutableList()
    }
}