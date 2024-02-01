package org.xiaoxigua.fakeplayer

import org.bukkit.scheduler.BukkitTask

class FakePlayerTask {
    enum class TaskType {
        Attack
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
}