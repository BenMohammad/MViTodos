package com.benmohammad.mvitodos.taskdetail

import com.benmohammad.mvitodos.mvibase.MviAction

sealed class TaskDetailAction: MviAction {

    data class PopulateTaskAction(val taskId: String): TaskDetailAction()
    data class DeleteTaskAction(val taskId: String): TaskDetailAction()
    data class ActivateTaskAction(val taskId: String): TaskDetailAction()
    data class CompleteTaskAction(val taskId: String): TaskDetailAction()
}