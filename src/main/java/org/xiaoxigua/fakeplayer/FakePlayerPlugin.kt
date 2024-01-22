package org.xiaoxigua.fakeplayer

import org.bukkit.plugin.java.JavaPlugin
import org.xiaoxigua.fakeplayer.commands.*
import org.xiaoxigua.fakeplayer.events.FakePlayerDeathEvent
import org.xiaoxigua.fakeplayer.events.JoinInit

class FakePlayerPlugin : JavaPlugin() {
    private val fakePlayers = mutableListOf<FakePlayerEntity>()

    override fun onEnable() {
        val commandManager = CommandManager("fake")

        commandManager.addSubCommand(Spawn(fakePlayers), Kill(fakePlayers), TPHere(fakePlayers))
        server.pluginManager.registerEvents(FakePlayerDeathEvent(fakePlayers), this)
        server.pluginManager.registerEvents(JoinInit(fakePlayers), this)
        server.scheduler.scheduleSyncRepeatingTask(this, {
            fakePlayers.forEach {
                it.doTick()
            }
        }, 0L, 1L)
    }

    override fun onDisable() {
        // Plugin shutdown logic
    }
}
