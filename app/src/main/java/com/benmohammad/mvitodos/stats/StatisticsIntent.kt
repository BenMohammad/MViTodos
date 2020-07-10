package com.benmohammad.mvitodos.stats

import com.benmohammad.mvitodos.mvibase.MviIntent

sealed class StatisticsIntent: MviIntent {

    object InitialIntent: StatisticsIntent()
}