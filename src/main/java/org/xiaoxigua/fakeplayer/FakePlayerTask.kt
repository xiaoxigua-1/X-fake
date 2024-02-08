package org.xiaoxigua.fakeplayer

import org.bukkit.scheduler.BukkitTask

class FakePlayerTask {
    enum class TaskType {
        Attack,
        Rotate,
        Craft,
        Place,
    }

    private val tasks: MutableMap<TaskType, BukkitTask> = mutableMapOf()
    fun addTask(type: TaskType, task: BukkitTask) {
        tasks[type]?.cancel()
        tasks[type] = task
    }

    fun removeTask(type: TaskType) {
        tasks[type]?.cancel()
        tasks.remove(type)
    }

    fun removeAllTask() {
        tasks.forEach { (_, task) ->
            task.cancel()
        }
    }
}