package com.benmohammad.mvitodos.tasks

import com.benmohammad.mvitodos.data.Task
import com.benmohammad.mvitodos.mvibase.MviResult

sealed class TasksResult: MviResult {

    sealed class LoadTasksResult : TasksResult() {
        data class Success(val tasks: List<Task>, val filterTYpe: TasksFilterType?): LoadTasksResult()
        data class Failure(val error: Throwable) : LoadTasksResult()
        object InFlight: LoadTasksResult()
    }

    sealed class ActivateTaskResult: TasksResult() {
        data class Success(val tasks: List<Task>): ActivateTaskResult()
        data class Failure(val error: Throwable): ActivateTaskResult()
        object InFlight: ActivateTaskResult()
        object HideUiNotification: ActivateTaskResult()
    }

    sealed class CompleteTaskResult: TasksResult() {
        data class Success(val tasks: List<Task>): CompleteTaskResult()
        data class Failure(val error: Throwable): CompleteTaskResult()
        object InFlight: CompleteTaskResult()
        object HideUiNotification: CompleteTaskResult()
    }

    sealed class ClearCompleteTasksResult : TasksResult() {
        data class Success(val tasks: List<Task>): ClearCompleteTasksResult()
        data class Failure(val error: Throwable): ClearCompleteTasksResult()
        object InFlight: ClearCompleteTasksResult()
        object HideUiNotification: ClearCompleteTasksResult()
    }
}