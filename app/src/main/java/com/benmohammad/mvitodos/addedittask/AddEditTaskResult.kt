package com.benmohammad.mvitodos.addedittask

import com.benmohammad.mvitodos.data.Task
import com.benmohammad.mvitodos.mvibase.MviResult

sealed class AddEditTaskResult: MviResult {

    sealed class PopulateTaskResult: AddEditTaskResult() {
        data class Success(val task: Task): PopulateTaskResult()
        data class Failure(val error: Throwable): PopulateTaskResult()
        object InFlight: PopulateTaskResult()
    }

    sealed class CreateTaskResult: AddEditTaskResult() {
        object Success: CreateTaskResult()
        object Empty: CreateTaskResult()
    }

    object UpdateTaskResult: AddEditTaskResult()
}