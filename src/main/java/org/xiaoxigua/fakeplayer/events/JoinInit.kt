package org.xiaoxigua.fakeplayer.events

import org.bukkit.craftbukkit.v1_20_R3.entity.CraftPlayer
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.xiaoxigua.fakeplayer.FakePlayerEntity

class JoinInit(private val fakePlayers: MutableList<FakePlayerEntity>) : Listener {
    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        val connection = (event.player as CraftPlayer).handle.connection

        fakePlayers.forEach {
            it.sendFakePlayerPacket(connection)
        }
    }
}