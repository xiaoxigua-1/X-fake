package org.xiaoxigua.fakeplayer

import com.mojang.authlib.GameProfile
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.PacketFlow
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ClientInformation
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.network.CommonListenerCookie
import net.minecraft.server.network.ServerGamePacketListenerImpl
import net.minecraft.world.phys.Vec3
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Server
import org.bukkit.World
import org.bukkit.craftbukkit.v1_20_R3.CraftWorld
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftPlayer
import org.bukkit.entity.Player
import org.xiaoxigua.fakeplayer.network.EmptyConnection

class FakePlayerEntity(
    server: Server,
    world: World,
    profile: GameProfile,
    val clientInfo: ClientInformation = ClientInformation.createDefault()
) :
    ServerPlayer(server as MinecraftServer, world as ServerLevel, profile, clientInfo) {

    private val world = (world as ServerLevel)

    fun spawn(spawnWorld: World, location: Location) {
        val spawnServerLevel = (spawnWorld as CraftWorld).handle

        connection = object : ServerGamePacketListenerImpl(
            server, EmptyConnection(PacketFlow.CLIENTBOUND), this,
            CommonListenerCookie(gameProfile, 0, clientInfo)
        ) {
            override fun send(packet: Packet<*>) {
            }
        }

        server.playerList.placeNewPlayer(EmptyConnection(PacketFlow.CLIENTBOUND), this, CommonListenerCookie.createInitial(gameProfile))

        world.removePlayerImmediately(this, RemovalReason.CHANGED_DIMENSION)
        unsetRemoved()
        setLevel(spawnServerLevel)
        spawnServerLevel.addRespawnedPlayer(this)
        setPos(Vec3(location.toVector().toVector3f()))
        addTag("fakePlayer")
        setLoadViewDistance(10)

        server.server.onlinePlayers.forEach { player ->
            sendFakePlayerPacket(player)
        }
    }

    fun sendFakePlayerPacket(player: Player) {
        val connection = (player as CraftPlayer).handle.connection

        sendFakePlayerPacket(connection)
    }

    private fun sendFakePlayerPacket(connection: ServerGamePacketListenerImpl) {
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

    fun remove() {
        Bukkit.getOnlinePlayers().forEach {
            val connection = (it as CraftPlayer).handle.connection

            connection.send(
                ClientboundPlayerInfoRemovePacket(
                    listOf(uuid)
                )
            )

            connection.send(
                ClientboundPlayerInfoUpdatePacket(
                    ClientboundPlayerInfoUpdatePacket.Action.UPDATE_LISTED,
                    this
                )
            )

            connection.send(
                ClientboundRemoveEntitiesPacket(
                    id,
                )
            )
        }

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
}