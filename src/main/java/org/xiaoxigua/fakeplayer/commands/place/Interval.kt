package org.xiaoxigua.fakeplayer.commands.place

import org.bukkit.command.CommandSender
import org.bukkit.scheduler.BukkitRunnable
import org.xiaoxigua.fakeplayer.*

class Interval(override val fakePlayers: MutableList<FakePlayerEntity>) : SubCommand() {
    override val name = "interval"
    override val description = "set fake player periodic place block"


    override fun onCommand(
        sender: CommandSender,
        commandArgs: MutableList<String>,
        args: MutableList<String>
    ): Boolean {
        val name = args.first()
        val fakePlayer =
            fakePlayers.find { it.displayName == name } ?: throw CommandError.CommandFakePlayerNotFound(name)
        val task = object : BukkitRunnable() {
            override fun run() {
                val selectedItem = fakePlayer.inventory.getSelected()

                if (!selectedItem.isEmpty && selectedItem.bukkitStack.type.isBlock)
                    fakePlayer.place(selectedItem.bukkitStack.type)
            }
        }.runTaskTimer(FakePlayerPlugin.currentPlugin!!, 0L, 5L)

        fakePlayer.taskManager.addTask(FakePlayerTask.TaskType.Place, task)

        return super.onCommand(sender, commandArgs, args)
    }
}