package org.xiaoxigua.fakeplayer.commands.craft

import org.xiaoxigua.fakeplayer.FakePlayerEntity
import org.xiaoxigua.fakeplayer.SubCommand

class Craft(override val fakePlayers: MutableList<FakePlayerEntity>) : SubCommand() {
    override val name = "craft"
    override val description = "set fake player craft action"

    init {
        addSubCommand(::Auto)
    }
}