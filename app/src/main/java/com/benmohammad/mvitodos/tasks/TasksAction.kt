package com.benmohammad.mvitodos.tasks

import com.benmohammad.mvitodos.data.Task
import com.benmohammad.mvitodos.mvibase.MviAction

sealed class TasksAction : MviAction {

    data class LoadTasksAction(
        val forceUpdate: Boolean,
        val filterType: TasksFilterType?
    ) : TasksAction()

    data class ActivateTasksAction(val task: Task) : TasksAction()

    data class CompleteTasksAction(val task: Task) : TasksAction()

    object ClearCompletedTasksAction : TasksAction()
}