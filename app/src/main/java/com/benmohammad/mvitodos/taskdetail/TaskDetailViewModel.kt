package com.benmohammad.mvitodos.taskdetail

import androidx.lifecycle.ViewModel
import com.benmohammad.mvitodos.mvibase.MviViewModel
import com.benmohammad.mvitodos.tasks.TasksActionProcessorHolder
import io.reactivex.Observable

class TaskDetailViewModel(
    private val annotationProcessorHolder: TasksActionProcessorHolder
): ViewModel(), MviViewModel<TaskDetailIntent, TaskDetailViewState> {

    override fun processIntents(intents: Observable<TaskDetailIntent>) {
        TODO("Not yet implemented")
    }

    override fun states(): Observable<TaskDetailViewState> {
        TODO("Not yet implemented")
    }
}