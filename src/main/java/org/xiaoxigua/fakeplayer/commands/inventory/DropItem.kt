package org.xiaoxigua.fakeplayer.commands.inventory

import net.minecraft.world.item.ItemStack
import org.bukkit.command.CommandSender
import org.xiaoxigua.fakeplayer.CommandError
import org.xiaoxigua.fakeplayer.FakePlayerEntity
import org.xiaoxigua.fakeplayer.SubCommand

class DropItem(override val fakePlayers: MutableList<FakePlayerEntity>) : SubCommand() {
    override val name = "drop"
    override val description = "fake player drop select item"

    override fun onCommand(
        sender: CommandSender,
        commandArgs: MutableList<String>,
        args: MutableList<String>
    ): Boolean {
        val arg = commandArgs.firstOrNull() ?: throw CommandError.CommandMissingArg("Item or all")
        val name = args.first()
        val fakePlayer =
            fakePlayers.find { it.displayName == name } ?: throw CommandError.CommandFakePlayerNotFound(name)

        if (arg == "all") {
            for (content in fakePlayer.inventory.compartments) {
                content.forEachIndexed { index, itemStack ->
                    if (!itemStack.isEmpty) {
                        fakePlayer.drop(itemStack, false, false)
                        content[index] = ItemStack.EMPTY
                    }
                }
            }
        } else fakePlayer.inventory.items.find { it.bukkitStack.type.toString() == arg && !it.bukkitStack.type.isAir }
            .let {
                fakePlayer.drop(it!!, false, false)
                fakePlayer.inventory.removeItem(it)
            }

        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        commandArgs: MutableList<String>,
        args: MutableList<String>
    ): MutableList<String> {
        val name = args.first()
        val fakePlayer = fakePlayers.find { it.displayName == name }
        val items =
            fakePlayer?.inventory?.items?.filter { !it.bukkitStack.type.isAir }?.map { it.bukkitStack.type.toString() }
                ?: listOf()

        return listOf("all").plus(items).toMutableList()
    }
}