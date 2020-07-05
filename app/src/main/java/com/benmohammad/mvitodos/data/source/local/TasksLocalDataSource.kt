package com.benmohammad.mvitodos.data.source.local

import android.content.Context
import com.benmohammad.mvitodos.data.Task
import com.benmohammad.mvitodos.data.source.TasksDataSource
import com.benmohammad.mvitodos.util.schedulers.BaseSchedulerProvider
import io.reactivex.Completable
import io.reactivex.Single

class TasksLocalDataSource private constructor(
    context: Context,
    schedulerProvider: BaseSchedulerProvider
): TasksDataSource {

    override fun getTasks(): Single<List<Task>> {
        TODO("Not yet implemented")
    }

    override fun getTask(taskId: String): Single<Task> {
        TODO("Not yet implemented")
    }

    override fun saveTask(task: Task): Completable {
        TODO("Not yet implemented")
    }

    override fun completeTask(task: Task): Completable {
        TODO("Not yet implemented")
    }

    override fun completeTask(taskId: String): Completable {
        TODO("Not yet implemented")
    }

    override fun activateTask(task: Task): Completable {
        TODO("Not yet implemented")
    }

    override fun activateTask(taskId: String): Completable {
        TODO("Not yet implemented")
    }

    override fun clearCompleted(): Completable {
        TODO("Not yet implemented")
    }

    override fun refreshTasks() {
        TODO("Not yet implemented")
    }

    override fun deleteAllTasks() {
        TODO("Not yet implemented")
    }

    override fun deleteTask(taskId: String): Completable {
        TODO("Not yet implemented")
    }
}