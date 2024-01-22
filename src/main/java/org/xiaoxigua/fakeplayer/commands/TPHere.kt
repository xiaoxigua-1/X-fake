package org.xiaoxigua.fakeplayer.commands

import net.minecraft.world.phys.Vec3
import org.bukkit.command.CommandSender
import org.bukkit.craftbukkit.v1_20_R3.CraftWorld
import org.bukkit.entity.Player
import org.xiaoxigua.fakeplayer.FakePlayerEntity
import org.xiaoxigua.fakeplayer.SubCommand

class TPHere(private val fakePlayers: MutableList<FakePlayerEntity>) : SubCommand() {
    override val name = "tphere"

    override fun onCommand(sender: CommandSender, args: MutableList<String>): Boolean {
        val name = args.removeFirst()

        if (args.isEmpty() && sender is Player) {
            fakePlayers.find { it.displayName == name }
                ?.tp((sender.world as CraftWorld).handle, Vec3(sender.location.toVector().toVector3f()))
        }

        return true
    }

    override fun onTabComplete(sender: CommandSender, args: MutableList<String>): MutableList<String> {
        return fakePlayers.map { it.displayName }.toMutableList()
    }
}