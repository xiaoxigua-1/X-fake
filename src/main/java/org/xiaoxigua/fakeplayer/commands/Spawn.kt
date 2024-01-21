package org.xiaoxigua.fakeplayer.commands

import com.mojang.authlib.GameProfile
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
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
            val gameProfile = GameProfile(UUID.randomUUID(), name)
            val fakePlayer = FakePlayerEntity(server, world, gameProfile)

            fakePlayer.spawn(sender.location)
            fakePlayers.add(fakePlayer)
            sender.server.onlinePlayers.forEach {
                it.sendMessage(Component.text("${fakePlayer.displayName} Joined the game", NamedTextColor.YELLOW))
            }
        }

        return true
    }

    override fun onTabComplete(sender: CommandSender, args: MutableList<String>): MutableList<String> {
        return mutableListOf("fake_player", "xiao")
    }
}