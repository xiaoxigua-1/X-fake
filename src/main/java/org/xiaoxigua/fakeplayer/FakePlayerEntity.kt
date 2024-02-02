package org.xiaoxigua.fakeplayer

import com.mojang.authlib.GameProfile
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.PacketFlow
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket
import net.minecraft.network.protocol.game.ClientboundAnimatePacket
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket
import net.minecraft.server.level.ClientInformation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.network.CommonListenerCookie
import net.minecraft.server.network.ServerGamePacketListenerImpl
import net.minecraft.world.phys.Vec3
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Server
import org.bukkit.World
import org.bukkit.craftbukkit.v1_20_R3.CraftServer
import org.bukkit.craftbukkit.v1_20_R3.CraftWorld
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftEntity
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftPlayer
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerRespawnEvent
import org.xiaoxigua.fakeplayer.network.EmptyConnection

class FakePlayerEntity(server: Server, world: World, profile: GameProfile, val clientInfo: ClientInformation = ClientInformation.createDefault()) : ServerPlayer((server as CraftServer).server, (world as CraftWorld).handle, profile, clientInfo) {

    private val world = (world as CraftWorld).handle
    val taskManager = FakePlayerTask()

    fun spawn(spawnWorld: World, location: Location) {
        val spawnServerLevel = (spawnWorld as CraftWorld).handle

        connection = object : ServerGamePacketListenerImpl(server, EmptyConnection(PacketFlow.CLIENTBOUND), this, CommonListenerCookie(gameProfile, 0, clientInfo)) {
            override fun send(packet: Packet<*>) {
            }
        }

        server.playerList.placeNewPlayer(EmptyConnection(PacketFlow.CLIENTBOUND), this, CommonListenerCookie.createInitial(gameProfile))
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
        connection.send(ClientboundAddEntityPacket(
                this,
        ))
    }

    private fun sendRemoveFakePlayerPacket(connection: ServerGamePacketListenerImpl) {
        connection.send(ClientboundPlayerInfoRemovePacket(listOf(uuid)))

        connection.send(ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_LISTED, this))

        connection.send(ClientboundRemoveEntitiesPacket(
                id,
        ))
    }

    private fun sendFakePlayerAttackAnimation(connection: ServerGamePacketListenerImpl) {
        connection.send(ClientboundAnimatePacket(bukkitEntity.handleRaw!!, 0))
    }

    private fun sendAllPlayerPacket(vararg sendPacket: (ServerGamePacketListenerImpl) -> Unit) {
        Bukkit.getOnlinePlayers().forEach {
            val connection = (it as CraftPlayer).handle.connection

            sendPacket.forEach {
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
            sendAllPlayerPacket(::sendFakePlayerAttackAnimation)
        }
    }
}