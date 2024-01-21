package org.xiaoxigua.fakeplayer

import org.bukkit.command.CommandSender

interface SubCommand {
    val name: String
    fun onCommand(sender: CommandSender, args: MutableList<String>): Boolean
}