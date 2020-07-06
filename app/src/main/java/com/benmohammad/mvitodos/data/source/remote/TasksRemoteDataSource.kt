package com.benmohammad.mvitodos.data.source.remote

import com.benmohammad.mvitodos.data.Task
import com.benmohammad.mvitodos.data.source.TasksDataSource
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import java.util.concurrent.TimeUnit

object TasksRemoteDataSource: TasksDataSource {

    private const val SERVICE_LATENCY_IN_MILLIS = 5000
    private val taskServiceData: MutableMap<String, Task>

    init {
        taskServiceData = LinkedHashMap(2)
        addTask("Build tower in Pisa", "Ground looks good, no foundation work required.")
        addTask("Finish bridge in Tacoma", "Found awesome girders at half the cost!")
    }

    private fun addTask(title: String, description: String) {
        val newTask = Task(title = title, description = description)
        taskServiceData.put(newTask.id, newTask)
    }

    override fun getTasks(): Single<List<Task>> {
        return Observable.fromIterable(taskServiceData.values)
            .delay(SERVICE_LATENCY_IN_MILLIS.toLong(), TimeUnit.MILLISECONDS)
            .toList()
    }

    override fun getTask(taskId: String): Single<Task> {
        return Single.just<Task>(taskServiceData[taskId])
            .delay(SERVICE_LATENCY_IN_MILLIS.toLong(), TimeUnit.MILLISECONDS)
    }

    override fun saveTask(task: Task): Completable {
        taskServiceData.put(task.id, task)
        return Completable.complete()
    }

    override fun completeTask(task: Task): Completable {
        val completedTask = Task(task.title!!, task.description, task.id, true)
        taskServiceData.put(task.id, completedTask)
        return Completable.complete()
    }

    override fun completeTask(taskId: String): Completable {
        return Completable.complete()
    }

    override fun activateTask(task: Task): Completable {
        val activeTask = Task(title = task.title!!, description = task.description!!, id = task.id)
        taskServiceData.put(task.id, activeTask)
        return Completable.complete()
    }

    override fun activateTask(taskId: String): Completable {
        return Completable.complete()
    }

    override fun clearCompletedTasks(): Completable {
        val it = taskServiceData.entries.iterator()
        while(it.hasNext()) {
            val entry = it.next()
            if(entry.value.completed) {
                it.remove()
            }
        }
        return Completable.complete()
    }

    override fun refreshTasks() {

    }

    override fun deleteAllTasks() {
        taskServiceData.clear()
    }

    override fun deleteTask(taskId: String): Completable {
        taskServiceData.remove(taskId)
        return Completable.complete()
    }
}