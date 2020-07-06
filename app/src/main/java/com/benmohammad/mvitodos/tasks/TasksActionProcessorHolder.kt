package com.benmohammad.mvitodos.tasks

import com.benmohammad.mvitodos.data.source.TasksRepository
import com.benmohammad.mvitodos.tasks.TasksAction.*
import com.benmohammad.mvitodos.tasks.TasksResult.*
import com.benmohammad.mvitodos.util.pairWithDelay
import com.benmohammad.mvitodos.util.schedulers.BaseSchedulerProvider
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import java.lang.IllegalArgumentException

class TasksActionProcessorHolder(
    private val tasksRepository: TasksRepository,
    private val schedulerProvider: BaseSchedulerProvider
) {

    private val loadTasksProcessor =
        ObservableTransformer<LoadTasksAction, LoadTasksResult> { actions ->
            actions.flatMap { action ->
                tasksRepository.getTasks(action.forceUpdate)
                    .toObservable()
                    .map { tasks -> LoadTasksResult.Success(tasks, action.filterTYpe) }
                    .cast(LoadTasksResult::class.java)
                    .onErrorReturn(LoadTasksResult::Failure)
                    .subscribeOn(schedulerProvider.io())
                    .observeOn(schedulerProvider.ui())
                    .startWith(TasksResult.LoadTasksResult.InFlight)
            }
        }

    private val activateTaskProcessor =
        ObservableTransformer<ActivateTasksAction, ActivateTaskResult> { actions ->
            actions.flatMap { action ->
                tasksRepository.activateTask(action.task)
                    .andThen(tasksRepository.getTasks())
                    .toObservable()
                    .flatMap { tasks ->
                        pairWithDelay(
                            TasksResult.ActivateTaskResult.Success(tasks),
                            TasksResult.ActivateTaskResult.HideUiNotification)
                    }
                            .onErrorReturn(ActivateTaskResult::Failure)
                            .subscribeOn(schedulerProvider.io())
                            .observeOn(schedulerProvider.ui())
                            .startWith(TasksResult.ActivateTaskResult.InFlight)
                    }
            }

    private val completeTaskProcessor =
        ObservableTransformer<CompleteTasksAction, CompleteTaskResult> { actions ->
            actions.flatMap { action ->
                tasksRepository.completeTask(action.task)
                    .andThen(tasksRepository.getTasks())
                    .toObservable()
                    .flatMap{tasks ->
                        pairWithDelay(
                            TasksResult.CompleteTaskResult.Success(tasks),
                            TasksResult.CompleteTaskResult.HideUiNotification)
                    }
                    .onErrorReturn(CompleteTaskResult::Failure)
                    .subscribeOn(schedulerProvider.io())
                    .observeOn(schedulerProvider.ui())
                    .startWith(TasksResult.CompleteTaskResult.InFlight)
            }
        }

    private val clearCompletedTaskProcessor =
        ObservableTransformer<ClearCompletedTasksAction, ClearCompleteTasksResult> { actions ->
            actions.flatMap { action ->
                tasksRepository.clearCompletedTasks()
                    .andThen(tasksRepository.getTasks())
                    .toObservable()
                    .flatMap { tasks ->
                        pairWithDelay(
                            ClearCompleteTasksResult.Success(tasks),
                            ClearCompleteTasksResult.HideUiNotification
                        )
                    }
                    .onErrorReturn(ClearCompleteTasksResult::Failure)
                    .subscribeOn(schedulerProvider.io())
                    .observeOn(schedulerProvider.ui())
                    .startWith(TasksResult.ClearCompleteTasksResult.InFlight)
            }
        }

    internal var actionProcessor =
        ObservableTransformer<TasksAction, TasksResult> { actions ->
            actions.publish { shared ->
                Observable.merge(
                    shared.ofType(TasksAction.LoadTasksAction::class.java).compose(loadTasksProcessor),

                    shared.ofType(TasksAction.ActivateTasksAction::class.java).compose(activateTaskProcessor),

                    shared.ofType(TasksAction.CompleteTasksAction::class.java).compose(completeTaskProcessor),

                    shared.ofType(TasksAction.ClearCompletedTasksAction::class.java).compose(clearCompletedTaskProcessor)
                ).mergeWith(
                    shared.filter{v -> v !is LoadTasksAction
                            && v !is ActivateTasksAction
                            && v !is CompleteTasksAction
                            && v !is ClearCompletedTasksAction
                    }.flatMap { w ->
                        Observable.error<TasksResult>(
                            IllegalArgumentException("Unknown Action type")
                        )
                    }
                )
            }
        }
}