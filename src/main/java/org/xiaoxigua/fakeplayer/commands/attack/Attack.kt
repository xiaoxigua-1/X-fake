package org.xiaoxigua.fakeplayer.commands.attack

import org.xiaoxigua.fakeplayer.FakePlayerEntity
import org.xiaoxigua.fakeplayer.SubCommand

class Attack(override val fakePlayers: MutableList<FakePlayerEntity>) : SubCommand() {
    override val name = "attack"
    override val description = "set fake player attack action"

    init {
        addSunCommand(::Interval)
    }
}