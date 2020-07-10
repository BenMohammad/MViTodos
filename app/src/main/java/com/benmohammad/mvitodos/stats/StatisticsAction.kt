package com.benmohammad.mvitodos.stats

import com.benmohammad.mvitodos.mvibase.MviAction

sealed class StatisticsAction: MviAction {

    object LoadStatisticsAction: StatisticsAction()
}