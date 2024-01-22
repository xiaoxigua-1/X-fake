package org.xiaoxigua.fakeplayer

import com.mojang.authlib.GameProfile
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.PacketFlow
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ClientInformation
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.network.CommonListenerCookie
import net.minecraft.server.network.ServerGamePacketListenerImpl
import net.minecraft.world.phys.Vec3
import org.bukkit.Location
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftPlayer
import org.xiaoxigua.fakeplayer.network.EmptyConnection

class FakePlayerEntity(
    server: MinecraftServer,
    private val world: ServerLevel,
    profile: GameProfile,
    val clientInfo: ClientInformation = ClientInformation.createDefault()
) :
    ServerPlayer(server, world, profile, clientInfo) {

    fun spawn(location: Location) {
        addTag("fakePlayer")
        setLoadViewDistance(10)

        connection = object : ServerGamePacketListenerImpl(
            server, EmptyConnection(PacketFlow.CLIENTBOUND), this,
            CommonListenerCookie(gameProfile, 0, clientInfo)
        ) {
            override fun send(packet: Packet<*>) {
            }
        }

        server.playerList.placeNewPlayer(EmptyConnection(PacketFlow.CLIENTBOUND), this, CommonListenerCookie.createInitial(gameProfile))
        setPos(location.x, location.y, location.z)

        server.server.onlinePlayers.forEach { player ->
            val connection = (player as CraftPlayer).handle.connection

            sendFakePlayerPacket(connection)
        }
    }

    fun sendFakePlayerPacket(connection: ServerGamePacketListenerImpl) {
        // add player to list
        connection.send(
            ClientboundPlayerInfoUpdatePacket(
                ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER,
                this
            )
        )

        connection.send(
            ClientboundPlayerInfoUpdatePacket(
                ClientboundPlayerInfoUpdatePacket.Action.UPDATE_LISTED,
                this
            )
        )

        // make player spawn in world
        connection.send(
            ClientboundAddEntityPacket(
                this,
            )
        )
    }

    fun tp(tpWorld: ServerLevel, vec3: Vec3) {
        if (world.uuid == tpWorld.uuid) {
            teleportTo(world, vec3)
        } else {
            val world = serverLevel()

            world.removePlayerImmediately(this, RemovalReason.CHANGED_DIMENSION)
            unsetRemoved()
            setLevel(tpWorld)
            tpWorld.addDuringCommandTeleport(this)
            triggerDimensionChangeTriggers(world)
            setPos(vec3)
        }
    }
}