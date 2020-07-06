package com.benmohammad.mvitodos.tasks

import com.benmohammad.mvitodos.data.Task
import com.benmohammad.mvitodos.mvibase.MviIntent

sealed class TasksIntent: MviIntent {

    object InitialIntent: TasksIntent()

    data class RefreshIntent(val forceUpdate: Boolean): TasksIntent()

    data class ActivateTasksIntent(val task: Task): TasksIntent()

    data class CompleteTasksIntent(val task: Task): TasksIntent()

    object ClearCompletedTasksIntent : TasksIntent()

    data class ChangeFilterIntent(val filterType: TasksFilterType): TasksIntent()
}