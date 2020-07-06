package com.benmohammad.mvitodos.mvibase

import io.reactivex.Observable

interface MviViewModel<I : MviIntent, S: MviViewState> {

    fun processIntents(intents: Observable<I>)

    fun states(): Observable<S>
}