package com.benmohammad.mvitodos.stats

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.benmohammad.mvitodos.R
import com.benmohammad.mvitodos.mvibase.MviView
import com.benmohammad.mvitodos.util.ToDoViewModelFactory
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import kotlin.LazyThreadSafetyMode.NONE

class StatisticsFragment: Fragment(), MviView<StatisticsIntent, StatisticsViewState> {

    private lateinit var statisticsTV: TextView
    private val disposables = CompositeDisposable()
    private val viewModel: StatisticsViewModel by lazy(NONE) {
        ViewModelProvider(this, ToDoViewModelFactory.getInstance(requireContext())).get(StatisticsViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.statistics_frag, container, false)
            .also{
                statisticsTV = it.findViewById(R.id.statistics)
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bind()
    }

    private fun bind() {
        disposables.add(viewModel.states().subscribe{this.render(it)})
        viewModel.processIntents(intents())
    }

    override fun intents(): Observable<StatisticsIntent> = initialIntent()

    private fun initialIntent(): Observable<StatisticsIntent> {
        return Observable.just(StatisticsIntent.InitialIntent)
    }

    override fun render(state: StatisticsViewState) {
        if(state.isLoading) statisticsTV.text = getString(R.string.loading)
        if(state.error != null) {
            statisticsTV.text = resources.getString(R.string.statistics_error)
        }

        if(state.error == null && !state.isLoading) {
            showStatistics(state.activeCount, state.completedCount)
        }
    }

    private fun showStatistics(numberOfActiveTasks: Int, numberOfCompletedTasks: Int) {
        if(numberOfCompletedTasks == 0 && numberOfActiveTasks == 0) {
            statisticsTV.text = resources.getString(R.string.statistics_no_tasks)
        } else {
            val displayString = (resources.getString(R.string.statistics_active_tasks)
                    + " "
                    + numberOfActiveTasks
                    + " "
                    + resources.getString(R.string.statistics_completed_tasks)
                    + " "
                    + numberOfCompletedTasks)
            statisticsTV.text = displayString
        }
    }

    companion object {
        operator fun invoke(): StatisticsFragment = StatisticsFragment()
    }
}