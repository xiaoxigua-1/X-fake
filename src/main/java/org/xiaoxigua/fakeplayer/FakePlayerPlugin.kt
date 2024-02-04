package org.xiaoxigua.fakeplayer

import org.bukkit.plugin.java.JavaPlugin
import org.xiaoxigua.fakeplayer.commands.Kill
import org.xiaoxigua.fakeplayer.commands.Spawn
import org.xiaoxigua.fakeplayer.commands.TPHere
import org.xiaoxigua.fakeplayer.commands.attack.Attack
import org.xiaoxigua.fakeplayer.commands.craft.Craft
import org.xiaoxigua.fakeplayer.commands.rotate.Rotate
import org.xiaoxigua.fakeplayer.events.FakePlayerInWorldEvent

class FakePlayerPlugin : JavaPlugin() {
    private val fakePlayers = mutableListOf<FakePlayerEntity>()

    companion object {
        var currentPlugin: JavaPlugin? = null
    }

    override fun onEnable() {
        val commandManager = CommandManager("fake", fakePlayers)

        currentPlugin = this
        commandManager.addSubCommand(::Spawn, ::Kill, ::TPHere, ::Attack, ::Rotate, ::Craft)
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
