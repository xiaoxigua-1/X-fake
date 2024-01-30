package org.xiaoxigua.fakeplayer.commands

import com.mojang.authlib.GameProfile
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.xiaoxigua.fakeplayer.CommandError
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

        if (name.length > 16) {
            throw CommandError.CommandStringLimit(16, name.length)
        } else if (sender is Player && !fakePlayers.any { it.displayName == name }) {
            val location = sender.location
            val gameProfile = GameProfile(UUID.randomUUID(), name)
            val fakePlayer = FakePlayerEntity(sender.server, sender.world, gameProfile)

            fakePlayer.spawn(sender.world, location)
            fakePlayers.add(fakePlayer)
        }

        return true
    }

    override fun onTabComplete(sender: CommandSender, args: MutableList<String>): MutableList<String> {
        return mutableListOf("Alex", "fake_player")
    }
}