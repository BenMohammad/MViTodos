package com.benmohammad.mvitodos.stats

import com.benmohammad.mvitodos.mvibase.MviResult

sealed class StatisticsResult: MviResult {

    sealed class LoadStatisticsResult: StatisticsResult() {
        data class Success(val activeCount: Int, val completedCount: Int): LoadStatisticsResult()
        data class Failure(val error: Throwable): LoadStatisticsResult()
        object InFlight: LoadStatisticsResult()
    }
}