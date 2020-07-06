package com.benmohammad.mvitodos.tasks

import androidx.lifecycle.ViewModel
import com.benmohammad.mvitodos.data.Task
import com.benmohammad.mvitodos.mvibase.MviViewModel
import com.benmohammad.mvitodos.tasks.TasksResult.*
import com.benmohammad.mvitodos.tasks.TasksResult.CompleteTaskResult.Success
import com.benmohammad.mvitodos.tasks.TasksViewState.UiNotification.COMPLETE_TASKS_CLEARED
import com.benmohammad.mvitodos.tasks.TasksViewState.UiNotification.TASK_ACTIVATED
import com.benmohammad.mvitodos.util.notOfType
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import io.reactivex.subjects.PublishSubject

class TasksViewModel(
    private val actionProcessorHolder : TasksActionProcessorHolder
): ViewModel(), MviViewModel<TasksIntent, TasksViewState> {

    private val intentSubject: PublishSubject<TasksIntent> = PublishSubject.create()
    private val stateObservable: Observable<TasksViewState> = compose()
    private val disposables = CompositeDisposable()


    private val intentFilter: ObservableTransformer<TasksIntent, TasksIntent>
    get() = ObservableTransformer { intents ->
        intents.publish{ shared ->
            Observable.merge(
                shared.ofType(TasksIntent.InitialIntent::class.java).take(1),
                shared.notOfType(TasksIntent.InitialIntent::class.java)
            )
        }
    }

    override fun processIntents(intents: Observable<TasksIntent>) {
        disposables.add(intents.subscribe(intentSubject::onNext))
    }

    override fun states(): Observable<TasksViewState> = stateObservable

    private fun compose(): Observable<TasksViewState> {
        return intentSubject
            .compose(intentFilter)
            .map(this::actionFromIntent)
            .compose(actionProcessorHolder.actionProcessor)
            .scan(TasksViewState.idle(), reducer)
            .distinctUntilChanged()
            .replay(1)
            .autoConnect(0)
    }

    private fun actionFromIntent(intent: TasksIntent): TasksAction {
        return when (intent) {
            is TasksIntent.InitialIntent -> TasksAction.LoadTasksAction(true, TasksFilterType.ALL_TASKS)
            is TasksIntent.RefreshIntent -> TasksAction.LoadTasksAction(intent.forceUpdate, null)
            is TasksIntent.ActivateTasksIntent -> TasksAction.ActivateTasksAction(intent.task)
            is TasksIntent.CompleteTasksIntent -> TasksAction.CompleteTasksAction(intent.task)
            is TasksIntent.ClearCompletedTasksIntent -> TasksAction.ClearCompletedTasksAction
            is TasksIntent.ChangeFilterIntent -> TasksAction.LoadTasksAction(false, intent.filterType)
        }
    }

    override fun onCleared() {
        disposables.dispose()
    }

    companion object {
        private val reducer  = BiFunction{previousState: TasksViewState, result: TasksResult ->
            when(result) {
                is LoadTasksResult -> when (result) {
                    is LoadTasksResult.Success -> {
                        val filterType = result.filterTYpe ?: previousState.tasksFilterType
                        val tasks = filteredTasks(result.tasks, filterType)
                        previousState.copy(
                            isLoading = false,
                            tasks = tasks,
                            tasksFilterType = filterType
                        )
                    }
                    is LoadTasksResult.Failure -> previousState.copy(isLoading = false, error = result.error)
                    is LoadTasksResult.InFlight -> previousState.copy(isLoading = false)
                }
                is TasksResult.CompleteTaskResult -> when(result) {
                    is Success ->
                        previousState.copy(
                            uiNotification = TasksViewState.UiNotification.TASK_COMPLETE,
                            tasks = filteredTasks(result.tasks, previousState.tasksFilterType)
                        )
                    is TasksResult.CompleteTaskResult.Failure -> previousState.copy(error = result.error)
                    is TasksResult.CompleteTaskResult.InFlight -> previousState
                    is TasksResult.CompleteTaskResult.HideUiNotification ->
                        if(previousState.uiNotification == TasksViewState.UiNotification.TASK_COMPLETE) {
                            previousState.copy(uiNotification = null)
                        } else {
                            previousState
                        }
                }
                is ActivateTaskResult -> when (result) {
                    is ActivateTaskResult.Success ->
                        previousState.copy(
                            uiNotification = TASK_ACTIVATED,
                            tasks = filteredTasks(result.tasks, previousState.tasksFilterType)
                        )
                    is ActivateTaskResult.Failure -> previousState.copy(error = result.error)
                    is ActivateTaskResult.InFlight -> previousState
                    is ActivateTaskResult.HideUiNotification ->
                        if(previousState.uiNotification == TASK_ACTIVATED) {
                            previousState.copy(uiNotification = null)
                        } else {
                            previousState
                        }
                }
                is ClearCompleteTasksResult -> when (result) {
                    is ClearCompleteTasksResult.Success ->
                        previousState.copy(
                            uiNotification = COMPLETE_TASKS_CLEARED,
                            tasks = filteredTasks(result.tasks, previousState.tasksFilterType)
                        )
                    is ClearCompleteTasksResult.Failure -> previousState.copy(error = result.error)
                    is ClearCompleteTasksResult.InFlight -> previousState
                    is ClearCompleteTasksResult.HideUiNotification ->
                        if(previousState.uiNotification == COMPLETE_TASKS_CLEARED) {
                            previousState.copy(uiNotification = null)
                        } else {
                            previousState
                        }
                }
            }
        }

        private fun filteredTasks(
            tasks: List<Task>,
            filterTYpe: TasksFilterType
            ): List<Task> {
            return when (filterTYpe) {
                TasksFilterType.ALL_TASKS -> tasks
                TasksFilterType.ACTIVE_TASKS -> tasks.filter(Task::active)
                TasksFilterType.COMPLETED_TASKS -> tasks.filter(Task::completed)
            }
        }
    }
}