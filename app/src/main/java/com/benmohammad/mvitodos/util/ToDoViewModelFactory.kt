package com.benmohammad.mvitodos.util

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.benmohammad.mvitodos.Injection
import com.benmohammad.mvitodos.addedittask.AddEditTaskActionProcessorHolder
import com.benmohammad.mvitodos.addedittask.AddEditTaskViewModel
import com.benmohammad.mvitodos.stats.StatisticsActionProcessorHolder
import com.benmohammad.mvitodos.stats.StatisticsViewModel
import com.benmohammad.mvitodos.taskdetail.TaskDetailActionProcessorHolder
import com.benmohammad.mvitodos.taskdetail.TaskDetailViewModel
import com.benmohammad.mvitodos.tasks.TasksActionProcessorHolder
import com.benmohammad.mvitodos.tasks.TasksViewModel
import java.lang.IllegalArgumentException

class ToDoViewModelFactory private constructor(
    private val applicationContext: Context
): ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass == TasksViewModel::class.java) {
            return TasksViewModel(
                TasksActionProcessorHolder(
                    Injection.provideTasksRepository(applicationContext),
                    Injection.provideSchedulerProvider())) as T
        } else if(modelClass == TaskDetailViewModel::class.java) {
            return TaskDetailViewModel(
                TaskDetailActionProcessorHolder(
                    Injection.provideTasksRepository(applicationContext),
                    Injection.provideSchedulerProvider())) as T
        } else if(modelClass == AddEditTaskViewModel::class.java) {
            return AddEditTaskViewModel(
                AddEditTaskActionProcessorHolder(
                    Injection.provideTasksRepository(applicationContext),
                    Injection.provideSchedulerProvider())) as T
        } else {
            return StatisticsViewModel(
                StatisticsActionProcessorHolder(
                    Injection.provideTasksRepository(applicationContext),
                    Injection.provideSchedulerProvider())) as T
        }

        throw IllegalArgumentException("Unknown model class " + modelClass)
    }

    companion object : SingletonHolderSingleArg<ToDoViewModelFactory, Context>(::ToDoViewModelFactory)
}