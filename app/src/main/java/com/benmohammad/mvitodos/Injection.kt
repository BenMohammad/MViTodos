package com.benmohammad.mvitodos

import android.content.Context
import com.benmohammad.mvitodos.data.source.TasksRepository
import com.benmohammad.mvitodos.data.source.local.TasksLocalDataSource
import com.benmohammad.mvitodos.data.source.remote.TasksRemoteDataSource
import com.benmohammad.mvitodos.util.schedulers.BaseSchedulerProvider
import com.benmohammad.mvitodos.util.schedulers.SchedulerProvider

object Injection {

    fun provideTasksRepository(context: Context): TasksRepository {
        return TasksRepository.getInstance(
            TasksRemoteDataSource,
            TasksLocalDataSource.getInstance(context, provideSchedulerProvider())
        )
    }
    fun provideSchedulerProvider(): BaseSchedulerProvider = SchedulerProvider
}