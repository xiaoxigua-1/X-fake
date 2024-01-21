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
        setPos(location.x, location.y, location.z)
        addTag("fakePlayer")

        connection = object : ServerGamePacketListenerImpl(
            server, EmptyConnection(PacketFlow.CLIENTBOUND), this,
            CommonListenerCookie(gameProfile, 0, clientInfo)
        ) {
            override fun send(packet: Packet<*>) {
            }
        }

        world.addNewPlayer(this)

        server.server.onlinePlayers.forEach { player ->
            val connection = (player as CraftPlayer).handle.connection

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
    }

}