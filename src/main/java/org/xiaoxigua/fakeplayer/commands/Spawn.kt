package org.xiaoxigua.fakeplayer.commands

import com.mojang.authlib.GameProfile
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.minecraft.world.phys.Vec3
import org.bukkit.command.CommandSender
import org.bukkit.craftbukkit.v1_20_R3.CraftServer
import org.bukkit.craftbukkit.v1_20_R3.CraftWorld
import org.bukkit.entity.Player
import org.xiaoxigua.fakeplayer.FakePlayerEntity
import org.xiaoxigua.fakeplayer.SubCommand
import java.util.*



class Spawn(private val fakePlayers: MutableList<FakePlayerEntity>) : SubCommand() {
    override val name = "spawn"

    override fun onCommand(
        sender: CommandSender,
        args: MutableList<String>
    ): Boolean {
        val name = args.removeFirst()

        if (sender is Player && !fakePlayers.any { it.displayName == name }) {
            val server = (sender.server as CraftServer).server
            val world = (sender.world as CraftWorld).handle
            val location = sender.location
            val gameProfile = GameProfile(UUID.randomUUID(), name)
            val fakePlayer = FakePlayerEntity(server, world, gameProfile)

            fakePlayer.spawn(world, location)
            fakePlayers.add(fakePlayer)
        }

        return true
    }

    override fun onTabComplete(sender: CommandSender, args: MutableList<String>): MutableList<String> {
        return mutableListOf("fake_player", "xiao")
    }
}