package com.benmohammad.mvitodos.mvibase

import com.benmohammad.mvitodos.addedittask.AddEditTaskIntent
import io.reactivex.Observable

interface MviView<I : MviIntent, in S : MviViewState> {

    fun intents(): Observable<I>

    fun render(state: S)
}