package org.xiaoxigua.fakeplayer

import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import org.xiaoxigua.fakeplayer.commands.*
import org.xiaoxigua.fakeplayer.commands.attack.Attack
import org.xiaoxigua.fakeplayer.commands.breaking.BreakBlock
import org.xiaoxigua.fakeplayer.commands.craft.Craft
import org.xiaoxigua.fakeplayer.commands.inventory.Inventory
import org.xiaoxigua.fakeplayer.commands.look.Look
import org.xiaoxigua.fakeplayer.commands.place.Place
import org.xiaoxigua.fakeplayer.events.FakePlayerInWorldEvent

class FakePlayerPlugin : JavaPlugin() {
    private val fakePlayers = mutableListOf<FakePlayerEntity>()

    companion object {
        var currentPlugin: JavaPlugin? = null
        val logger = Bukkit.getLogger()
    }

    override fun onEnable() {
        val commandManager = CommandManager("fake", fakePlayers)

        currentPlugin = this
        commandManager.addSubCommand(
            ::Spawn,
            ::Kill,
            ::TPHere,
            ::Attack,
            ::Look,
            ::Craft,
            ::Stop,
            ::Place,
            ::BreakBlock,
            ::Inventory,
            ::Mount
        )
        server.pluginManager.registerEvents(FakePlayerInWorldEvent(fakePlayers), this)
        server.scheduler.scheduleSyncRepeatingTask(this, {
            fakePlayers.forEach {
                it.doTick()
            }
        }, 0L, 1L)
    }

    override fun onDisable() {
        fakePlayers.forEach {
            it.remove()
        }
        fakePlayers.clear()
    }
}
