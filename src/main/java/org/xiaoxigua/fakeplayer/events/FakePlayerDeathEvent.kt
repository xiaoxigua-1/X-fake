package org.xiaoxigua.fakeplayer.events

import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket
import net.minecraft.world.entity.Entity
import org.bukkit.Bukkit
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.craftbukkit.v1_20_R3.CraftWorld
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftPlayer
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.xiaoxigua.fakeplayer.FakePlayerEntity

class FakePlayerDeathEvent(private val fakePlayers: MutableList<FakePlayerEntity>) : Listener {
    @EventHandler
    fun onPlayerDeath(event: PlayerDeathEvent) {
        val world = (event.player.world as CraftWorld).handle
        val fakePlayer = fakePlayers.find { it.uuid == event.player.uniqueId }

        if (fakePlayer != null) {
            Bukkit.getOnlinePlayers().forEach {
                val connection = (it as CraftPlayer).handle.connection

                connection.send(
                    ClientboundPlayerInfoRemovePacket(
                        listOf(fakePlayer.uuid)
                    )
                )

                connection.send(
                    ClientboundPlayerInfoUpdatePacket(
                        ClientboundPlayerInfoUpdatePacket.Action.UPDATE_LISTED,
                        fakePlayer
                    )
                )

                connection.send(
                    ClientboundRemoveEntitiesPacket(
                        fakePlayer.id,
                    )
                )
            }

            fakePlayers.remove(fakePlayer)
            world.removePlayerImmediately(fakePlayer, Entity.RemovalReason.KILLED)
            event.deathMessage(Component.text("${fakePlayer.displayName} left the game", NamedTextColor.YELLOW))
        }
    }
}