package org.xiaoxigua.fakeplayer.commands

import net.minecraft.world.phys.Vec3
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.xiaoxigua.fakeplayer.CommandError
import org.xiaoxigua.fakeplayer.FakePlayerEntity
import org.xiaoxigua.fakeplayer.SubCommand

class TPHere(override val fakePlayers: MutableList<FakePlayerEntity>) : SubCommand() {
    override val name = "tphere"
    override val description = "tp fake player to your position"

    override fun onCommand(sender: CommandSender, args: MutableList<String>): Boolean {
        val name = args.removeFirst()

        if (sender is Player) {
            fakePlayers.find { it.displayName == name }
                    ?.tp(sender.world, Vec3(sender.location.toVector().toVector3f()))
                    ?: throw CommandError.CommandFakePlayerNotFound(name)
        }

        return true
    }
}