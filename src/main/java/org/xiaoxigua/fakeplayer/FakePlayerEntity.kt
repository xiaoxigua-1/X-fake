package org.xiaoxigua.fakeplayer

import com.mojang.authlib.GameProfile
import net.minecraft.core.BlockPos
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.PacketFlow
import net.minecraft.network.protocol.game.*
import net.minecraft.server.level.ClientInformation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.network.CommonListenerCookie
import net.minecraft.server.network.ServerGamePacketListenerImpl
import net.minecraft.world.phys.Vec3
import org.bukkit.*
import org.bukkit.block.Block
import org.bukkit.craftbukkit.v1_20_R3.CraftServer
import org.bukkit.craftbukkit.v1_20_R3.CraftWorld
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftEntity
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftPlayer
import org.bukkit.entity.Player
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import org.bukkit.event.player.PlayerLoginEvent
import org.bukkit.event.player.PlayerRespawnEvent
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask
import org.bukkit.util.BlockIterator
import org.bukkit.util.BoundingBox
import org.xiaoxigua.fakeplayer.network.EmptyConnection
import java.net.InetAddress

class FakePlayerEntity(
    server: Server,
    world: World,
    profile: GameProfile,
    val clientInfo: ClientInformation = ClientInformation.createDefault()
) : ServerPlayer((server as CraftServer).server, (world as CraftWorld).handle, profile, clientInfo) {

    private val world = (world as CraftWorld).handle
    val taskManager = FakePlayerTask()

    fun spawn(spawnWorld: World, location: Location) {
        val spawnServerLevel = (spawnWorld as CraftWorld).handle

        connection = object : ServerGamePacketListenerImpl(
            server,
            EmptyConnection(PacketFlow.CLIENTBOUND),
            this,
            CommonListenerCookie(gameProfile, 0, clientInfo)
        ) {
            override fun send(packet: Packet<*>) {}
        }

        triggerPlayerLoginEvent()

        // add fake player to server player list
        server.playerList.placeNewPlayer(
            EmptyConnection(PacketFlow.CLIENTBOUND),
            this,
            CommonListenerCookie.createInitial(gameProfile)
        )
        server.playerList.respawn(this, false, PlayerRespawnEvent.RespawnReason.DEATH)

        world.removePlayerImmediately(this, RemovalReason.CHANGED_DIMENSION)
        unsetRemoved()
        setLevel(spawnServerLevel)
        spawnServerLevel.addRespawnedPlayer(this)
        setPos(Vec3(location.toVector().toVector3f()))
        setRot(location.yaw, location.pitch)
        addTag("fakePlayer")
        setLoadViewDistance(10)
        sendAllPlayerPacket(::sendFakePlayerPacket)
    }

    // trigger player join event
    private fun triggerPlayerLoginEvent() {
        val bukkitScheduler = Bukkit.getScheduler()

        bukkitScheduler.runTaskAsynchronously(FakePlayerPlugin.currentPlugin!!) { _ ->
            val hostName = "0.0.0.0"
            val inetAddress = InetAddress.getByName(hostName)
            val pluginManager = Bukkit.getPluginManager()

            pluginManager
                .callEvent(AsyncPlayerPreLoginEvent(displayName, inetAddress, uuid))
            bukkitScheduler.runTask(FakePlayerPlugin.currentPlugin!!) { _ ->
                pluginManager.callEvent(PlayerLoginEvent(bukkitEntity.player!!, hostName, inetAddress))
            }
        }
    }

    fun sendFakePlayerPacket(player: Player) {
        val connection = (player as CraftPlayer).handle.connection

        sendFakePlayerPacket(connection)
    }

    private fun sendFakePlayerPacket(connection: ServerGamePacketListenerImpl) {
        // add player to list
        connection.send(ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER, this))

        // update player list
        connection.send(ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_LISTED, this))

        // make player spawn in world
        connection.send(
            ClientboundAddEntityPacket(
                this,
            )
        )
    }

    private fun sendRemoveFakePlayerPacket(connection: ServerGamePacketListenerImpl) {
        connection.send(ClientboundPlayerInfoRemovePacket(listOf(uuid)))

        connection.send(ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_LISTED, this))

        connection.send(
            ClientboundRemoveEntitiesPacket(
                id,
            )
        )
    }

    private fun sendFakePlayerSwingMainArmAnimation(connection: ServerGamePacketListenerImpl) {
        connection.send(ClientboundAnimatePacket(bukkitEntity.handleRaw!!, 0))
    }

    private fun sendFakePlayerBreakBlockAnimation(connection: ServerGamePacketListenerImpl, pos: BlockPos, progress: Int) {
        connection.send(ClientboundBlockDestructionPacket(id, pos, progress))
    }

    private fun sendAllPlayerPacket(vararg sendPacket: (ServerGamePacketListenerImpl) -> Unit) {
        sendAllPlayerPacket(Bukkit.getOnlinePlayers(), *sendPacket)
    }

    private fun sendNearPlayerPacket(vararg sendPacket: (ServerGamePacketListenerImpl) -> Unit) {
        val location = bukkitEntity.location
        sendAllPlayerPacket(bukkitEntity.world.getNearbyPlayers(location, 160.0, 320.0, 160.0), *sendPacket)
    }

    private fun sendAllPlayerPacket(
        players: Collection<Player>,
        vararg sendPaket: (ServerGamePacketListenerImpl) -> Unit
    ) {
        players.forEach { player ->
            val connection = (player as CraftPlayer).handle.connection

            sendPaket.forEach {
                it(connection)
            }
        }
    }

    fun remove() {
        sendAllPlayerPacket(::sendRemoveFakePlayerPacket)
        taskManager.removeAllTask()
        inventory.dropAll()
        world.removePlayerImmediately(this, RemovalReason.KILLED)
        world.craftServer.handle.remove(this)
    }

    fun tp(tpWorld: World, vec3: Vec3) {
        val tpWorldLevel = (tpWorld as CraftWorld).handle

        if (world.uuid == tpWorldLevel.uuid) {
            teleportTo(world, vec3)
        } else {
            val world = serverLevel()

            world.removePlayerImmediately(this, RemovalReason.CHANGED_DIMENSION)
            unsetRemoved()
            setLevel(tpWorldLevel)
            tpWorldLevel.addDuringCommandTeleport(this)
            triggerDimensionChangeTriggers(world)
            setPos(vec3)
        }
    }

    fun attack() {
        val attackTarget = this.bukkitEntity.player?.getTargetEntity(3)

        if (attackTarget != null) {
            attack((attackTarget as CraftEntity).handle)
            sendNearPlayerPacket(::sendFakePlayerSwingMainArmAnimation)
        }
    }

    fun place(blockType: Material) {
        val blockIterator = BlockIterator(bukkitEntity, 4)
        val distancesBlock = mutableListOf(blockIterator.next())
        val world = bukkitEntity.world
        var lastBlock: Location? = null

        while (blockIterator.hasNext()) {
            distancesBlock.add(blockIterator.next())
        }


        for (index in 2..<distancesBlock.size) {
            println("${distancesBlock[index]}")
            if (!distancesBlock[index].isPassable && !distancesBlock[index].isEmpty && !distancesBlock[index].isLiquid) {
                if (distancesBlock[index - 1].canPlace(blockType.createBlockData()))
                    lastBlock = distancesBlock[index - 1].location
                break
            }
        }

        if (lastBlock != null && blockType.isBlock && world.getNearbyEntities(
                BoundingBox(
                    lastBlock.x,
                    lastBlock.y,
                    lastBlock.z,
                    lastBlock.x + 1,
                    lastBlock.y + 1,
                    lastBlock.z + 1
                )
            ).isEmpty()
        ) {
            inventory.getSelected().count -= 1
            world.getBlockAt(lastBlock).type = blockType
        }

        sendNearPlayerPacket(::sendFakePlayerSwingMainArmAnimation)
    }

    fun breaking(): BukkitTask {
        var breakBlock: Block? = null
        var progress = 0f
        var lastProgress = 0
        val task = object : BukkitRunnable() {
            override fun run() {
                val blockIterator = BlockIterator(bukkitEntity, 4)

                while (blockIterator.hasNext()) {
                    val nextBlock = blockIterator.next()

                    if (!nextBlock.type.isAir && (breakBlock == null || breakBlock?.type == nextBlock.type)) {
                        breakBlock = nextBlock
                        break
                    } else if (!nextBlock.type.isAir && breakBlock?.type != nextBlock.type) {
                        breakBlock = null
                        break
                    }
                }

                progress += breakBlock?.getBreakSpeed(bukkitEntity) ?: 0f
                sendNearPlayerPacket(::sendFakePlayerSwingMainArmAnimation)

                // send block breaking animation packet to near players
                if (breakBlock != null && (progress * 10).toInt() > lastProgress) {
                    lastProgress = (progress * 10).toInt()
                    sendNearPlayerPacket({ connection ->
                        sendFakePlayerBreakBlockAnimation(connection, BlockPos(breakBlock!!.x, breakBlock!!.y, breakBlock!!.z), lastProgress)
                    })
                }

                if (progress >= 1f) {
                    breakBlock?.breakNaturally(inventory.getSelected().bukkitStack)
                    cancel()
                } else if (breakBlock == null) {
                    cancel()
                }
            }
        }.runTaskTimer(FakePlayerPlugin.currentPlugin!!, 0L, 1L)

        return task
    }

    fun riding() {
        val iterator = BlockIterator(bukkitEntity, 4)

        for (block in iterator) {
            if (block.type.isAir || listOf(
                    block.boundingBox.widthX,
                    block.boundingBox.widthZ,
                    block.boundingBox.height
                ).any { it < 1 }
            ) {
                bukkitEntity.world.getNearbyEntities(
                    BoundingBox(
                        block.location.x,
                        block.location.y,
                        block.location.z,
                        block.location.x + 1,
                        block.location.y + 1,
                        block.location.z + 1
                    )
                ).takeIf { it.isNotEmpty() }?.iterator()?.next()?.takeIf { it.entityId != id }
                    ?.addPassenger(bukkitEntity) ?: continue
                break
            } else break
        }
    }
}