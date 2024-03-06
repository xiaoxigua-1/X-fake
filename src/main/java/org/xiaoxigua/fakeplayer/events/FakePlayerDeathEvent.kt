package org.xiaoxigua.fakeplayer.events

import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import org.xiaoxigua.fakeplayer.FakePlayerEntity

class FakePlayerInWorldEvent(private val fakePlayers: MutableList<FakePlayerEntity>) : Listener {
    @EventHandler
    fun onFakePlayerDeath(event: EntityDamageEvent) {
        val fakePlayer = fakePlayers.find { it.uuid == event.entity.uniqueId }
        val entity = event.entity

        if (fakePlayer != null && entity is Player && entity.health - event.finalDamage <= 0) {
            event.isCancelled = true
            fakePlayer.remove()
            fakePlayers.remove(fakePlayer)
        }
    }
}