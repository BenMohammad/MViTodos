package com.benmohammad.mvitodos.data.source.local

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.text.TextUtils
import com.benmohammad.mvitodos.data.Task
import com.benmohammad.mvitodos.data.source.TasksDataSource
import com.benmohammad.mvitodos.data.source.local.TasksPersistenceContract.TaskEntry.COLUMN_NAME_COMPLETED
import com.benmohammad.mvitodos.data.source.local.TasksPersistenceContract.TaskEntry.COLUMN_NAME_DESCRIPTION
import com.benmohammad.mvitodos.data.source.local.TasksPersistenceContract.TaskEntry.COLUMN_NAME_ENTRY_ID
import com.benmohammad.mvitodos.data.source.local.TasksPersistenceContract.TaskEntry.COLUMN_NAME_TITLE
import com.benmohammad.mvitodos.data.source.local.TasksPersistenceContract.TaskEntry.TABLE_NAME
import com.benmohammad.mvitodos.util.SingletonHolderDoubleArg
import com.benmohammad.mvitodos.util.schedulers.BaseSchedulerProvider
import com.squareup.sqlbrite2.BriteDatabase
import com.squareup.sqlbrite2.SqlBrite
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.functions.Function

class TasksLocalDataSource private constructor(
    context: Context,
    schedulerProvider: BaseSchedulerProvider
): TasksDataSource {

    private val databaseHelper: BriteDatabase
    private val taskMapperFunction: Function<Cursor, Task>

    init {
        val dbHelper = TasksDbHelper(context)
        val sqlBrite = SqlBrite.Builder().build()
        databaseHelper = sqlBrite.wrapDatabaseHelper(dbHelper, schedulerProvider.io())
        taskMapperFunction = Function { this.getTask(it)}
    }

    private fun getTask(c: Cursor): Task {
        val itemId = c.getString(c.getColumnIndexOrThrow(COLUMN_NAME_ENTRY_ID))
        val title = c.getString(c.getColumnIndexOrThrow(COLUMN_NAME_TITLE))
        val description = c.getString(c.getColumnIndexOrThrow(COLUMN_NAME_DESCRIPTION))
        val completed = c.getInt(c.getColumnIndexOrThrow(COLUMN_NAME_COMPLETED)) == 1
        return Task(
            title = title,
            description = description,
            id = itemId,
            completed = completed)
    }

    override fun getTasks(): Single<List<Task>> {
        val projection = arrayOf(
            COLUMN_NAME_ENTRY_ID, COLUMN_NAME_TITLE,
            COLUMN_NAME_DESCRIPTION, COLUMN_NAME_COMPLETED
        )

        val sql = String.format("SELECT * FROM %s", TextUtils.join(", ", projection), TABLE_NAME)

        return databaseHelper
            .createQuery(TABLE_NAME, sql)
            .mapToList(taskMapperFunction)
            .firstOrError()
    }

    override fun getTask(taskId: String): Single<Task> {
        val projection = arrayOf(
            COLUMN_NAME_ENTRY_ID, COLUMN_NAME_TITLE,
            COLUMN_NAME_DESCRIPTION, COLUMN_NAME_COMPLETED
        )

        val sql = String.format("SELECT * FROM %s WHERE %s LIKE ?", TextUtils.join(", ", projection),
            TABLE_NAME, COLUMN_NAME_ENTRY_ID
        )

        return databaseHelper
            .createQuery(TABLE_NAME, sql, taskId)
            .mapToOne(taskMapperFunction)
            .firstOrError()
    }

    override fun saveTask(task: Task): Completable {
        val values = ContentValues()
        values.put(COLUMN_NAME_ENTRY_ID, task.id)
        values.put(COLUMN_NAME_TITLE, task.title)
        values.put(COLUMN_NAME_DESCRIPTION, task.description)
        values.put(COLUMN_NAME_COMPLETED, task.completed)
        databaseHelper.insert(TABLE_NAME, values, SQLiteDatabase.CONFLICT_REPLACE)
        return Completable.complete()
    }

    override fun completeTask(task: Task): Completable {
        completeTask(task.id)
        return Completable.complete()
    }

    override fun completeTask(taskId: String): Completable {
        val values = ContentValues()
        values.put(COLUMN_NAME_COMPLETED, true)
        val selection = COLUMN_NAME_ENTRY_ID + " LIKE ?"
        val selectionArgs = arrayOf(taskId)
        databaseHelper.update(TABLE_NAME, values, selection, *selectionArgs)
        return Completable.complete()
    }

    override fun activateTask(task: Task): Completable {
        activateTask(task.id)
        return Completable.complete()
    }

    override fun activateTask(taskId: String): Completable {
        val values = ContentValues()
        values.put(COLUMN_NAME_COMPLETED, false)
        val selection = COLUMN_NAME_ENTRY_ID + " LIKE ?"
        val selectionArgs = arrayOf(taskId)
        databaseHelper.update(TABLE_NAME, values, selection, *selectionArgs)
        return Completable.complete()
    }

    override fun clearCompletedTasks(): Completable {
        val selection = COLUMN_NAME_COMPLETED + " LIKE ?"
        val selectionArgs = arrayOf("1")
        databaseHelper.delete(TABLE_NAME, selection, *selectionArgs)
        return Completable.complete()
    }

    override fun refreshTasks() {

    }

    override fun deleteAllTasks() {
        databaseHelper.delete(TABLE_NAME, null)
    }

    override fun deleteTask(taskId: String): Completable {
        val selection = COLUMN_NAME_ENTRY_ID + " LIKE ?"
        val selectionArgs = arrayOf(taskId)
        databaseHelper.delete(TABLE_NAME, selection, *selectionArgs)
        return Completable.complete()
    }

    companion object: SingletonHolderDoubleArg<TasksLocalDataSource, Context, BaseSchedulerProvider>(
        ::TasksLocalDataSource
    )
}