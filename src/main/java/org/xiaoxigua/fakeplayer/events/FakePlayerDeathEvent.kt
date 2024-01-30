package org.xiaoxigua.fakeplayer.events

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.xiaoxigua.fakeplayer.FakePlayerEntity

class FakePlayerInWorldEvent(private val fakePlayers: MutableList<FakePlayerEntity>) : Listener {
    @EventHandler
    fun onFakePlayerDeath(event: PlayerDeathEvent) {
        val fakePlayer = fakePlayers.find { it.uuid == event.player.uniqueId }

        if (fakePlayer != null) {
            fakePlayer.remove()
            fakePlayers.remove(fakePlayer)
            event.deathMessage(Component.text("${fakePlayer.displayName} left the game", NamedTextColor.YELLOW))
        }
    }

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        fakePlayers.forEach {
            it.sendFakePlayerPacket(event.player)
        }
    }
}