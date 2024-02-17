package org.xiaoxigua.fakeplayer.events

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.xiaoxigua.fakeplayer.FakePlayerEntity

class FakePlayerInWorldEvent(private val fakePlayers: MutableList<FakePlayerEntity>) : Listener {
    @EventHandler
    fun onFakePlayerDeath(event: PlayerDeathEvent) {
        val fakePlayer = fakePlayers.find { it.uuid == event.player.uniqueId }

        if (fakePlayer != null) {
            fakePlayer.remove()
            fakePlayers.remove(fakePlayer)

            event.isCancelled = true
        }
    }
}