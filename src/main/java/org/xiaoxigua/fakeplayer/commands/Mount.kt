package org.xiaoxigua.fakeplayer.commands

import org.bukkit.command.CommandSender
import org.bukkit.entity.EntityType
import org.xiaoxigua.fakeplayer.CommandError
import org.xiaoxigua.fakeplayer.FakePlayerEntity
import org.xiaoxigua.fakeplayer.SubCommand

class Mount(override val fakePlayers: MutableList<FakePlayerEntity>) : SubCommand() {
    override val name = "mount"
    override val description = "fake player rides a near by thing which is rideable"

    override fun onCommand(
        sender: CommandSender,
        commandArgs: MutableList<String>,
        args: MutableList<String>
    ): Boolean {
        val name = args.removeFirst()
        val fakePlayer =
            fakePlayers.find { it.displayName == name } ?: throw CommandError.CommandFakePlayerNotFound(name)
        val nearEntity = fakePlayer.bukkitEntity.getNearbyEntities(5.0, 5.0, 5.0).filter { entity ->
                when (entity.type) {
                    EntityType.MINECART, EntityType.BOAT, EntityType.HORSE, EntityType.ZOMBIE_HORSE, EntityType.SKELETON_HORSE, EntityType.DONKEY, EntityType.CAMEL, EntityType.LLAMA, EntityType.TRADER_LLAMA, EntityType.PIG, EntityType.STRIDER, EntityType.MULE, EntityType.CHEST_BOAT -> true

                    else -> false
                }
            }.random()

        nearEntity.addPassenger(fakePlayer.bukkitEntity)

        return true
    }
}