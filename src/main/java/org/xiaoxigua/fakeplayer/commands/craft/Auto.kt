package org.xiaoxigua.fakeplayer.commands.craft

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.command.CommandSender
import org.bukkit.craftbukkit.v1_20_R3.inventory.CraftItemStack
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.RecipeChoice
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.inventory.ShapelessRecipe
import org.bukkit.scheduler.BukkitRunnable
import org.xiaoxigua.fakeplayer.*

class Auto(override val fakePlayers: MutableList<FakePlayerEntity>) : SubCommand() {
    override val name = "auto"
    override val description = "set fake player auto crafting"

    private val items = Material.entries.map { it.name.lowercase() }

    override fun onCommand(
        sender: CommandSender,
        commandArgs: MutableList<String>,
        args: MutableList<String>
    ): Boolean {
        val name = args.first()
        val itemName = commandArgs.firstOrNull() ?: throw CommandError.CommandMissingArg("Item")
        val fakePlayer =
            fakePlayers.find { it.displayName == name } ?: throw CommandError.CommandFakePlayerNotFound(name)
        val item =
            itemName.takeIf { items.contains(it) }
                ?: throw CommandError.CommandItemNotFound(itemName)
        val itemStack = ItemStack(Material.getMaterial(item.uppercase())!!)
        val recipes = getRecipes(itemStack)

        if (recipes.isNotEmpty()) {
            val task = object : BukkitRunnable() {
                override fun run() {
                    recipes.forEach { recipe ->
                        val ingredients = mutableListOf<net.minecraft.world.item.ItemStack>()
                        val drops = mutableListOf<net.minecraft.world.item.ItemStack>()

                        // if inventory is recipe ingredient add to ingredients variable
                        for (i in fakePlayer.inventory.items) {
                            if (recipe.values.any { recipeIngredients -> recipeIngredients.any { it.type == i.bukkitStack.type } })
                                if (ingredients.any { it.bukkitStack.type == i.bukkitStack.type }) {
                                    ingredients.find { it.bukkitStack.type == i.bukkitStack.type }!!.count += i.count
                                } else ingredients.add(i.copy())
                            else if (i.bukkitStack.type != Material.AIR)
                                drops.add(i.copy())
                        }

                        for ((recipeResult, recipeIngredients) in recipe) {
                            var minResult = 9999
                            val result = recipeResult.clone()

                            // calculate min crafting count
                            recipeIngredients.forEach { recipeIngredientItem ->
                                val resultCount =
                                    (ingredients.find { it.bukkitStack.type == recipeIngredientItem.type }?.count
                                        ?: 0) / recipeIngredientItem.amount

                                if (minResult > resultCount)
                                    minResult = resultCount
                            }

                            recipeIngredients.forEach { recipeIngredientItem ->
                                val nowIngredient =
                                    ingredients.find { it.bukkitStack.type == recipeIngredientItem.type }

                                if (nowIngredient != null && nowIngredient.count - recipeIngredientItem.amount * minResult > 0) {
                                    nowIngredient.count -= recipeIngredientItem.amount * minResult
                                } else if (nowIngredient != null) {
                                    ingredients.remove(nowIngredient)
                                }
                            }

                            result.amount *= minResult

                            if (result.amount != 0)
                                drops.add(CraftItemStack.asNMSCopy(result))
                        }

                        // Drop crafting result and irrelevant items
                        drops.forEach { fakePlayer.drop(it, false, false) }

                        for (i in 0..35) {
                            fakePlayer.inventory.items[i] =
                                ingredients.getOrNull(i) ?: net.minecraft.world.item.ItemStack.EMPTY
                        }
                    }
                }
            }.runTaskTimer(FakePlayerPlugin.currentPlugin!!, 0L, 2L)

            fakePlayer.taskManager.addTask(FakePlayerTask.TaskType.Craft, task)
        } else throw CommandError.CommandItemCantCraft(item)

        return true
    }

    private fun getRecipes(itemStack: ItemStack): List<Map<ItemStack, MutableList<ItemStack>>> {
        return Bukkit.getServer().getRecipesFor(itemStack).filter { it is ShapedRecipe || it is ShapelessRecipe }
            .map { recipe ->
                val ingredients = mutableListOf<ItemStack>()

                (if (recipe is ShapedRecipe)
                    recipe.choiceMap.values
                else
                    (recipe as ShapelessRecipe).choiceList)
                    .filterNotNull()
                    .map { (it as RecipeChoice.MaterialChoice).itemStack }
                    .forEach { item ->
                        if (ingredients.any { it.type == item.type }) {
                            ingredients.find { it.type == item.type }!!.amount += 1
                        } else ingredients.add(item)
                    }

                mapOf(recipe.result to ingredients)
            }
    }

    override fun onTabComplete(sender: CommandSender, commandArgs: MutableList<String>): MutableList<String> {
        return items.toMutableList()
    }
}