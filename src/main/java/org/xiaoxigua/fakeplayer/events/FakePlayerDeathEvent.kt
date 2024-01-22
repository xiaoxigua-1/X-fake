package org.xiaoxigua.fakeplayer.events

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.xiaoxigua.fakeplayer.FakePlayerEntity

class FakePlayerDeathEvent(private val fakePlayers: MutableList<FakePlayerEntity>) : Listener {
    @EventHandler
    fun onPlayerDeath(event: PlayerDeathEvent) {
        val fakePlayer = fakePlayers.find { it.uuid == event.player.uniqueId }

        if (fakePlayer != null) {
            fakePlayer.remove()
            fakePlayers.remove(fakePlayer)
            event.deathMessage(Component.text("${fakePlayer.displayName} left the game", NamedTextColor.YELLOW))
        }
    }
}