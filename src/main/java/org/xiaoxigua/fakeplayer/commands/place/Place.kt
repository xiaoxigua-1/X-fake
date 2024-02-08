package org.xiaoxigua.fakeplayer.commands.place

import net.minecraft.world.item.ItemStack
import org.bukkit.command.CommandSender
import org.xiaoxigua.fakeplayer.CommandError
import org.xiaoxigua.fakeplayer.FakePlayerEntity
import org.xiaoxigua.fakeplayer.SubCommand

class Place(override val fakePlayers: MutableList<FakePlayerEntity>) : SubCommand() {
    override val name = "place"
    override val description = "set fake player place block"

    init {
        addSubCommand(::Interval)
    }

    override fun onCommand(
        sender: CommandSender,
        commandArgs: MutableList<String>,
        args: MutableList<String>
    ): Boolean {
        val name = args.first()
        val fakePlayer =
            fakePlayers.find { it.displayName == name } ?: throw CommandError.CommandFakePlayerNotFound(name)
        val selectedItem = fakePlayer.inventory.getSelected()

        if (selectedItem != ItemStack.EMPTY && selectedItem.bukkitStack.type.isBlock)
            fakePlayer.place(selectedItem.bukkitStack.type)
        return super.onCommand(sender, commandArgs, args)
    }
}