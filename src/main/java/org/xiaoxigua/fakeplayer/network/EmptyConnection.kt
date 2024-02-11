package org.xiaoxigua.fakeplayer.network

import net.minecraft.network.Connection
import net.minecraft.network.PacketListener
import net.minecraft.network.PacketSendListener
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.PacketFlow
import java.net.SocketAddress

class EmptyConnection(flag: PacketFlow) : Connection(flag) {
    init {
        channel = EmptyChannel(null)
        address = object : SocketAddress() {
            private val serialVersionUID = 8207338859896320185L
        }
    }

    override fun flushChannel() {}

    override fun isConnected(): Boolean {
        return true
    }

    override fun send(packet: Packet<*>) {}

    override fun send(packet: Packet<*>, genericfuturelistener: PacketSendListener?) {}

    override fun send(packet: Packet<*>, genericfuturelistener: PacketSendListener?, flag: Boolean) {}

    override fun setListener(pl: PacketListener) {
        try {
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }
}