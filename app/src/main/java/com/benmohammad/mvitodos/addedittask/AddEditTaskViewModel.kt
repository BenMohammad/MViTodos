package com.benmohammad.mvitodos.addedittask

import androidx.lifecycle.ViewModel
import com.benmohammad.mvitodos.addedittask.AddEditTaskAction.*
import com.benmohammad.mvitodos.addedittask.AddEditTaskResult.*
import com.benmohammad.mvitodos.mvibase.MviViewModel
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.disposables.CompositeDisposable

import io.reactivex.functions.BiFunction
import io.reactivex.subjects.PublishSubject

class AddEditTaskViewModel(
    private val actionProcessorHolder: AddEditTaskActionProcessorHolder
): ViewModel(), MviViewModel<AddEditTaskIntent, AddEditTaskViewState> {

    private val intentsSubject: PublishSubject<AddEditTaskIntent> = PublishSubject.create()
    private val stateObservable: Observable<AddEditTaskViewState> = compose()
    private val disposables = CompositeDisposable()

    private val intentFilter: ObservableTransformer<AddEditTaskIntent, AddEditTaskIntent>
    get() = ObservableTransformer { intents ->
        intents.publish{ shared ->
            Observable.merge<AddEditTaskIntent>(
                shared.ofType(AddEditTaskIntent.InitialIntent::class.java).take(1),
                shared.ofType(AddEditTaskIntent.InitialIntent::class.java)
            )
        }
    }

    private fun compose(): Observable<AddEditTaskViewState>{
        return intentsSubject
            .compose<AddEditTaskIntent>(intentFilter)
            .map<AddEditTaskAction>(this::actionFromIntent)
            // Special case where we do not want to pass this event down the stream
            .filter { action -> action !is AddEditTaskAction.SkipMe }
            .compose(actionProcessorHolder.actionProcessor)
            // Cache each state and pass it to the reducer to create a new state from
            // the previous cached one and the latest Result emitted from the action processor.
            // The Scan operator is used here for the caching.
            .scan(AddEditTaskViewState.idle(), reducer)
            // When a reducer just emits previousState, there's no reason to call render. In fact,
            // redrawing the UI in cases like this can cause jank (e.g. messing up snackbar animations
            // by showing the same snackbar twice in rapid succession).
            .distinctUntilChanged()
            // Emit the last one event of the stream on subscription
            // Useful when a View rebinds to the ViewModel after rotation.
            .replay(1)
            // Create the stream on creation without waiting for anyone to subscribe
            // This allows the stream to stay alive even when the UI disconnects and
            // match the stream's lifecycle to the ViewModel's one.
            .autoConnect(0)
    }

    override fun processIntents(intents: Observable<AddEditTaskIntent>) {
        disposables.add(intents.subscribe(intentsSubject::onNext))
    }

    override fun states(): Observable<AddEditTaskViewState> = stateObservable

    private fun actionFromIntent(intent: AddEditTaskIntent): AddEditTaskAction {
        return when (intent) {
            is AddEditTaskIntent.InitialIntent -> {
                if(intent.taskId == null) {
                    SkipMe
                } else {
                    PopulateTaskAction(taskId = intent.taskId)
                }
            }
            is AddEditTaskIntent.SaveTask -> {
                val(taskId, title, description) = intent
                if(taskId == null) {
                    CreateTaskAction(title, description)
                } else {
                    UpdateTaskAction(taskId, title, description)
                }
            }
        }
    }

    override fun onCleared() {
        disposables.dispose()
    }

    companion object {
        private val reducer = BiFunction{previousState: AddEditTaskViewState, result: AddEditTaskResult ->
            when(result) {
                is PopulateTaskResult -> when (result) {
                    is PopulateTaskResult.Success -> {
                        result.task.let { task ->
                            if(task.active) {
                            previousState.copy(title = task.title!!, description = task.description!!)
                        } else {
                            previousState
                        }
                        }
                    }
                    is PopulateTaskResult.Failure -> previousState.copy(error = result.error)
                    is PopulateTaskResult.InFlight -> previousState
                }
                is CreateTaskResult -> when(result) {
                    is CreateTaskResult.Success -> previousState.copy(isEmpty = false, isSaved = true)
                    is CreateTaskResult.Empty -> previousState.copy(isEmpty = true)
                }
                is UpdateTaskResult -> previousState.copy(isSaved = true)
            }
        }
    }
}