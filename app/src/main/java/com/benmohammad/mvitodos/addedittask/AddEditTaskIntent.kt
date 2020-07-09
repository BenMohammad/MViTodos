package com.benmohammad.mvitodos.addedittask

import com.benmohammad.mvitodos.mvibase.MviIntent

sealed class AddEditTaskIntent: MviIntent {

    data class InitialIntent(val taskId: String?): AddEditTaskIntent()
    data class SaveTask(
        val taskId: String?,
        val title: String,
        val description: String
    ): AddEditTaskIntent()
}