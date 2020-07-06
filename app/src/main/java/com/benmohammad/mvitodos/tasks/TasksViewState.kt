package com.benmohammad.mvitodos.tasks

import com.benmohammad.mvitodos.data.Task
import com.benmohammad.mvitodos.mvibase.MviViewState
import com.benmohammad.mvitodos.tasks.TasksFilterType.ALL_TASKS

data class TasksViewState(
    val isLoading: Boolean,
    val tasksFilterType: TasksFilterType,
    val tasks: List<Task>,
    val error: Throwable?,
    val uiNotification: UiNotification?
): MviViewState {

    enum class UiNotification {
        TASK_COMPLETE,
        TASK_ACTIVATED,
        COMPLETE_TASKS_CLEARED
    }

    companion object {
        fun idle(): TasksViewState {
            return TasksViewState(
                isLoading = false,
                tasksFilterType = ALL_TASKS,
                tasks = emptyList(),
                error = null,
                uiNotification = null
            )
        }
    }
}