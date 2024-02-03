package org.xiaoxigua.fakeplayer.commands.rotate

import org.xiaoxigua.fakeplayer.FakePlayerEntity
import org.xiaoxigua.fakeplayer.SubCommand

class Rotate(override val fakePlayers: MutableList<FakePlayerEntity>) : SubCommand() {
    override val name = "rotate"
    override val description = "set fake player rotate action"

    init {
        addSubCommand(::Linear, ::Look)
    }
}