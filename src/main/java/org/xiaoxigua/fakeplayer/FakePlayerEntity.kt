package org.xiaoxigua.fakeplayer

import com.destroystokyo.paper.profile.ProfileProperty
import com.google.gson.JsonParser
import com.mojang.authlib.GameProfile
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.minecraft.core.BlockPos
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.PacketFlow
import net.minecraft.network.protocol.game.ClientboundAnimatePacket
import net.minecraft.network.protocol.game.ClientboundBlockDestructionPacket
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
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import org.bukkit.event.player.PlayerLoginEvent
import org.bukkit.event.player.PlayerRespawnEvent
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask
import org.bukkit.util.BlockIterator
import org.bukkit.util.BoundingBox
import org.xiaoxigua.fakeplayer.network.EmptyConnection
import java.io.InputStreamReader
import java.net.InetAddress
import java.net.URL
import java.util.logging.Level


class FakePlayerEntity(
    server: Server,
    world: World,
    profile: GameProfile,
    val clientInfo: ClientInformation = ClientInformation.createDefault()
) : ServerPlayer((server as CraftServer).server, (world as CraftWorld).handle, profile, clientInfo) {

    val taskManager = FakePlayerTask()
    private val mojangApiURI = "https://api.mojang.com/"
    private val mojangSessionApiURI = "https://sessionserver.mojang.com"

    fun spawn(spawnWorld: World, location: Location): FakePlayerEntity {
        val spawnServerLevel = (spawnWorld as CraftWorld).handle
        val playerList = server.playerList

        connection = object : ServerGamePacketListenerImpl(
            server,
            EmptyConnection(PacketFlow.CLIENTBOUND),
            this,
            CommonListenerCookie(gameProfile, 0, clientInfo)
        ) {
            override fun send(packet: Packet<*>) {}
        }

        triggerPlayerLoginEvent()

        try {
            setTexture()
        } catch (e: Exception) {
            FakePlayerPlugin.logger.log(Level.WARNING, e.message)
        }

        // add fake player to server player list
        playerList.placeNewPlayer(
            EmptyConnection(PacketFlow.CLIENTBOUND),
            playerList.getPlayerForLogin(gameProfile, clientInfo, this),
            CommonListenerCookie.createInitial(gameProfile)
        )
        val fakePlayer = if (bukkitEntity.isDead)
            playerList.respawn(this, false, PlayerRespawnEvent.RespawnReason.DEATH)
        else this

        fakePlayer.teleportTo(spawnServerLevel, Vec3(location.toVector().toVector3f()))
        fakePlayer.addTag("fakePlayer")
        fakePlayer.setLoadViewDistance(10)

        return fakePlayer as FakePlayerEntity
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

    // get skin texture
    private fun setTexture() {
        val playerProfile = bukkitEntity.playerProfile

        val getUuidUrl = URL("$mojangApiURI/users/profiles/minecraft/$displayName")
        val uuidReader = InputStreamReader(getUuidUrl.openStream())
        val uuid = JsonParser.parseReader(uuidReader).asJsonObject.get("id").asString

        val getTextureUrl = URL("$mojangSessionApiURI/session/minecraft/profile/$uuid?unsigned=false")
        val textureReader = InputStreamReader(getTextureUrl.openStream())
        val properties =
            JsonParser.parseReader(textureReader).asJsonObject.get("properties").asJsonArray.get(0)
                .asJsonObject

        val value = properties["value"].asString
        val signature = properties["signature"].asString

        playerProfile.setProperty(ProfileProperty("textures", value, signature))
        bukkitEntity.playerProfile = playerProfile
    }

    private fun sendFakePlayerSwingMainArmAnimation(connection: ServerGamePacketListenerImpl) {
        connection.send(ClientboundAnimatePacket(bukkitEntity.handleRaw!!, 0))
    }

    private fun sendFakePlayerBreakBlockAnimation(
        connection: ServerGamePacketListenerImpl,
        pos: BlockPos,
        progress: Int
    ) {
        connection.send(ClientboundBlockDestructionPacket(id, pos, progress))
    }

    private fun sendNearPlayerPacket(vararg sendPacket: (ServerGamePacketListenerImpl) -> Unit) {
        val location = bukkitEntity.location
        sendAllPlayerPacket(bukkitEntity.world.getNearbyPlayers(location, 80.0, 160.0, 80.0), *sendPacket)
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
        taskManager.removeAllTask()
        inventory.dropAll()
        server.server.broadcast(Component.text("$displayName left the game", NamedTextColor.YELLOW))
        server.playerList.remove(this)
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
            if (!distancesBlock[index].isPassable && !distancesBlock[index].isEmpty && !distancesBlock[index].isLiquid) {
                if (distancesBlock[index - 1].canPlace(blockType.createBlockData()) && (distancesBlock[index - 1].type.isAir || distancesBlock[index - 1].isLiquid))
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
            ).isEmpty() && !blockType.isEmpty
        ) {
            inventory.removeFromSelected(false)
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
                        sendFakePlayerBreakBlockAnimation(
                            connection,
                            BlockPos(breakBlock!!.x, breakBlock!!.y, breakBlock!!.z),
                            lastProgress
                        )
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
                for (entity in bukkitEntity.world.getNearbyEntities(
                    BoundingBox(
                        block.location.x,
                        block.location.y,
                        block.location.z,
                        block.location.x + 1,
                        block.location.y + 1,
                        block.location.z + 1
                    )
                )) {
                    when (entity.type) {
                        EntityType.MINECART,
                        EntityType.BOAT,
                        EntityType.HORSE,
                        EntityType.ZOMBIE_HORSE,
                        EntityType.SKELETON_HORSE,
                        EntityType.DONKEY,
                        EntityType.CAMEL,
                        EntityType.LLAMA,
                        EntityType.TRADER_LLAMA,
                        EntityType.PIG,
                        EntityType.STRIDER,
                        EntityType.MULE,
                        EntityType.CHEST_BOAT -> entity.addPassenger(
                            bukkitEntity
                        )

                        else -> continue
                    }
                }
            }
            break
        }
    }
}