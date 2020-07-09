package com.benmohammad.mvitodos.addedittask

import com.benmohammad.mvitodos.mvibase.MviViewState

data class AddEditTaskViewState(
    val isEmpty: Boolean,
    val isSaved: Boolean,
    val title: String,
    val description: String,
    val error: Throwable?
): MviViewState {

    companion object {
        fun idle(): AddEditTaskViewState {
            return AddEditTaskViewState(
                title = "",
                description = "",
                error = null,
                isEmpty = false,
                isSaved = false
            )
        }

    }}