package org.xiaoxigua.fakeplayer.commands.inventory

import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.xiaoxigua.fakeplayer.CommandError
import org.xiaoxigua.fakeplayer.FakePlayerEntity
import org.xiaoxigua.fakeplayer.SubCommand
import org.xiaoxigua.fakeplayer.commands.inventory.dropItem.DropItem

class Inventory(override val fakePlayers: MutableList<FakePlayerEntity>) : SubCommand() {
    override val name = "inventory"
    override val description = "open fake player inventory gui"

    init {
        addSubCommand(::DropItem)
    }

    override fun onCommand(
        sender: CommandSender,
        commandArgs: MutableList<String>,
        args: MutableList<String>
    ): Boolean {
        val name = args.first()
        val fakePlayer =
            fakePlayers.find { it.displayName == name } ?: throw CommandError.CommandFakePlayerNotFound(name)

        if (sender is Player) {
            // open fake player inventory GUI
            sender.openInventory(fakePlayer.bukkitEntity.inventory)
        }
        return true
    }
}