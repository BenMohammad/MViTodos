package com.benmohammad.mvitodos.util

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.benmohammad.mvitodos.Injection
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
        }
        throw IllegalArgumentException("Unknown model class " + modelClass)
    }

    companion object : SingletonHolderSingleArg<ToDoViewModelFactory, Context>(::ToDoViewModelFactory)
}