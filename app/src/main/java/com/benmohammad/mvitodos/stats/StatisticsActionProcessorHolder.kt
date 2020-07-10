package com.benmohammad.mvitodos.stats

import com.benmohammad.mvitodos.data.Task
import com.benmohammad.mvitodos.data.source.TasksRepository
import com.benmohammad.mvitodos.stats.StatisticsResult.LoadStatisticsResult
import com.benmohammad.mvitodos.util.flatMapIterable
import com.benmohammad.mvitodos.util.schedulers.BaseSchedulerProvider
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.Single
import io.reactivex.functions.BiFunction

class StatisticsActionProcessorHolder(
    private val tasksRepository: TasksRepository,
    private val schedulerProvider: BaseSchedulerProvider
) {

    private val loadStatisticsProcessor =
        ObservableTransformer<StatisticsAction.LoadStatisticsAction, LoadStatisticsResult> {actions ->
            actions.flatMap {
                tasksRepository.getTasks()
                    .flatMapIterable()
                    .publish<LoadStatisticsResult.Success> { shared ->
                        Single.zip<Int, Int, LoadStatisticsResult.Success>(
                        shared.filter(Task::active).count().map(Long::toInt),
                        shared.filter(Task::completed).count().map(Long::toInt),
                        BiFunction{activeCount, completedCount ->
                            LoadStatisticsResult.Success(activeCount, completedCount)
                        }
                        ).toObservable()
                    }
                    .cast(LoadStatisticsResult::class.java)
                    .onErrorReturn(LoadStatisticsResult::Failure)
                    .subscribeOn(schedulerProvider.io())
                    .observeOn(schedulerProvider.ui())
                    .startWith(LoadStatisticsResult.InFlight)
            }
        }

    var actionProcessor =
        ObservableTransformer<StatisticsAction, StatisticsResult> { actions ->
            actions.publish { shared ->
                shared.ofType(StatisticsAction.LoadStatisticsAction::class.java).compose(loadStatisticsProcessor)
                    .cast(StatisticsResult::class.java)
                    .mergeWith(
                        shared.filter { v -> v !is StatisticsAction.LoadStatisticsAction }
                            .flatMap { w ->
                                Observable.error<StatisticsResult>(
                                    IllegalArgumentException("Unknown Action type:" + w)
                                )
                            }
                    )
            }
        }
}