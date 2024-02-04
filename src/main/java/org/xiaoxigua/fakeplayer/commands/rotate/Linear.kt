package org.xiaoxigua.fakeplayer.commands.rotate

import org.bukkit.command.CommandSender
import org.bukkit.scheduler.BukkitRunnable
import org.xiaoxigua.fakeplayer.*

class Linear(override val fakePlayers: MutableList<FakePlayerEntity>) : SubCommand() {
    override val name = "linear"
    override val description = "set fake player rotate at an even speed"

    override fun onCommand(
        sender: CommandSender,
        commandArgs: MutableList<String>,
        args: MutableList<String>
    ): Boolean {
        val name = args.first()
        val fakePlayer = fakePlayers.find { it.displayName == name }
            ?: throw CommandError.CommandFakePlayerNotFound(name)
        val yaw = getArg(commandArgs)
        val pitch = getArg(commandArgs)
        val task = object : BukkitRunnable() {
            override fun run() {
                val nowYaw = fakePlayer.bukkitEntity.yaw
                val nowPitch = fakePlayer.bukkitEntity.pitch

                fakePlayer.setRot(nowYaw + yaw, nowPitch + pitch)
            }
        }.runTaskTimer(FakePlayerPlugin.currentPlugin!!, 0L, 1L)

        fakePlayer.taskManager.addTask(FakePlayerTask.TaskType.Rotate, task)

        return true
    }

    override fun onTabComplete(sender: CommandSender, commandArgs: MutableList<String>): MutableList<String> {
        return if (commandArgs.size < 2) {
            (-179..179).map(Int::toString)
        } else {
            listOf()
        }.toMutableList()
    }

    private fun getArg(commandArgs: MutableList<String>): Float {
        val arg = commandArgs.removeFirstOrNull() ?: "10"

        return if (Regex("-?\\d+").matches(arg)) {
            arg.toFloat()
        } else {
            throw CommandError.CommandArgTypeError("Int")
        }
    }
}