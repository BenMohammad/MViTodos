package com.benmohammad.mvitodos.stats

import com.benmohammad.mvitodos.mvibase.MviViewState
import io.reactivex.internal.schedulers.NewThreadWorker

data class StatisticsViewState(
    val isLoading: Boolean,
    val activeCount: Int,
    val completedCount: Int,
    val error: Throwable?
): MviViewState {

    companion object {
        fun idle(): StatisticsViewState {
            return StatisticsViewState(
                isLoading = false,
                activeCount = 0,
                completedCount = 0,
                error = null
            )
        }

    }}