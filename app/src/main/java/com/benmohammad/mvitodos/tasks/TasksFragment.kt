package com.benmohammad.mvitodos.tasks

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.benmohammad.mvitodos.R
import com.benmohammad.mvitodos.addedittask.AddEditTaskActivity
import com.benmohammad.mvitodos.mvibase.MviView
import com.benmohammad.mvitodos.taskdetail.TaskDetailActivity
import com.benmohammad.mvitodos.tasks.TasksViewState.UiNotification.*
import com.benmohammad.mvitodos.util.ToDoViewModelFactory
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.rxbinding2.support.v4.widget.RxSwipeRefreshLayout
import com.jakewharton.rxbinding4.swiperefreshlayout.refreshes
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import kotlin.LazyThreadSafetyMode.NONE

class TasksFragment: Fragment(), MviView<TasksIntent, TasksViewState> {

    private lateinit var listAdapter: TasksAdapter
    private lateinit var fab: FloatingActionButton
    private lateinit var noTasksView: View
    private lateinit var noTaskIcon: ImageView
    private lateinit var noTaskMainView: TextView
    private lateinit var noTaskAddView: TextView
    private lateinit var tasksView: LinearLayout
    private lateinit var filteringLabelView: TextView
    private lateinit var swipeRefreshLayout: ScrollChildSwipeRefreshLayout
    private val refreshPublishIntent = PublishSubject.create<TasksIntent.RefreshIntent>()
    private val clearCompletedTaskIntentPublisher = PublishSubject.create<TasksIntent.ClearCompletedTasksIntent>()
    private val changeFilterIntentPublisher = PublishSubject.create<TasksIntent.ChangeFilterIntent>()
    private val disposables = CompositeDisposable()
    private val viewModel: TasksViewModel by lazy(NONE) {
        ViewModelProvider(this, ToDoViewModelFactory.getInstance(requireContext())).get(TasksViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        listAdapter = TasksAdapter(ArrayList(0))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bind()
    }

    private fun bind() {
        disposables.add(viewModel.states().subscribe(this::render))
        viewModel.processIntents(intents())
        disposables.add(listAdapter.taskClickObservable.subscribe { task -> showTaskDetailUi(task.id)})
    }

    override fun onResume() {
        super.onResume()
        refreshPublishIntent.onNext(TasksIntent.RefreshIntent(false))
    }

    override fun onDestroy() {
        super.onDestroy()
        disposables.dispose()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(AddEditTaskActivity.REQUEST_ADD_TASK == requestCode && Activity.RESULT_OK == resultCode) {
            showSuccessfullySavedMessage()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.tasks_frag, container, false)
        val listView = root.findViewById<ListView>(R.id.tasks_list)
        listView.adapter = listAdapter
        filteringLabelView = root.findViewById(R.id.filteringLabel)
        tasksView = root.findViewById(R.id.tasksLL)

        noTasksView = root.findViewById(R.id.noTasks)
        noTaskIcon = root.findViewById(R.id.noTasksIcon)
        noTaskMainView = root.findViewById(R.id.noTasksMain)
        noTaskAddView = root.findViewById(R.id.noTasksAdd)
        noTaskAddView.setOnClickListener { showAddTask() }

        fab = requireActivity()!!.findViewById(R.id.fab_add_task)

        fab.setImageResource(R.drawable.ic_add)
        fab.setOnClickListener { showAddTask() }

        swipeRefreshLayout = root.findViewById(R.id.refresh_layout)
        swipeRefreshLayout.setColorSchemeColors(
            ContextCompat.getColor(requireActivity()!!, R.color.colorPrimary),
            ContextCompat.getColor(requireActivity()!!, R.color.colorAccent),
            ContextCompat.getColor(requireActivity()!!, R.color.colorPrimaryDark)
        )

        swipeRefreshLayout.setScrollUpChild(listView)
        setHasOptionsMenu(true)
        return root
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.menu_clear ->
                clearCompletedTaskIntentPublisher.onNext(TasksIntent.ClearCompletedTasksIntent)

            R.id.menu_filter -> showFilteringPopupMenu()
            R.id.menu_refresh -> refreshPublishIntent.onNext(TasksIntent.RefreshIntent(true))
        }
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.tasks_fragment_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun showFilteringPopupMenu() {
        val popup = PopupMenu(requireContext(), requireActivity().findViewById(R.id.menu_filter))
        popup.menuInflater.inflate(R.menu.filter_tasks, popup.menu)
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.active -> changeFilterIntentPublisher.onNext(
                    TasksIntent.ChangeFilterIntent(TasksFilterType.ACTIVE_TASKS)
                )
                R.id.completed -> changeFilterIntentPublisher.onNext(
                    TasksIntent.ChangeFilterIntent(TasksFilterType.COMPLETED_TASKS)
                )
                else -> changeFilterIntentPublisher.onNext(TasksIntent.ChangeFilterIntent(TasksFilterType.ALL_TASKS))
            }
            true
        }
        popup.show()
    }

    private fun showMessage(message: String) {
        val view = view ?: return
        Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show()
    }

    private fun showSuccessfullySavedMessage() {
        showMessage(getString(R.string.successfully_saved_task_message))
    }

    private fun showAddTask() {
        val intent = Intent(context, AddEditTaskActivity::class.java)
        startActivityForResult(intent, AddEditTaskActivity.REQUEST_ADD_TASK)
    }

    private fun showTaskDetailUi(taskId: String) {
        val intent = Intent(context, TaskDetailActivity::class.java)
        intent.putExtra(TaskDetailActivity.EXTRA_TASK_ID, taskId)
        startActivity(intent)
    }





    override fun intents(): Observable<TasksIntent> {
        return Observable.merge(
            initialIntent(),
            refreshIntent(),
            adapterIntent(),
            clearCompletedTaskIntent()
        )
            .mergeWith(
                changeFilterIntent()
            )
    }

    private fun initialIntent(): Observable<TasksIntent.InitialIntent> {
        return Observable.just(TasksIntent.InitialIntent)
    }


    private fun clearCompletedTaskIntent(): Observable<TasksIntent.ClearCompletedTasksIntent> {
        return clearCompletedTaskIntentPublisher
    }

    private fun refreshIntent(): Observable<TasksIntent.RefreshIntent>? {
        return RxSwipeRefreshLayout.refreshes(swipeRefreshLayout)
            .map{ TasksIntent.RefreshIntent(false)}
            .mergeWith(refreshPublishIntent)
    }

    private fun changeFilterIntent(): Observable<TasksIntent.ChangeFilterIntent> {
        return changeFilterIntentPublisher
    }

    private fun adapterIntent(): Observable<TasksIntent> {
        return listAdapter.taskToggleObservable.map {
            task -> if(!task.completed) {
            TasksIntent.CompleteTasksIntent(task)
        } else {
            TasksIntent.ActivateTasksIntent(task)
        }
        }
    }



    override fun render(state: TasksViewState) {
        swipeRefreshLayout.isRefreshing = state.isLoading
        if(state.error != null) {
            showLoadingTasksError()
            return
        }

        when(state.uiNotification) {
            TASK_COMPLETE -> showMessage(getString(R.string.task_marked_complete))
            TASK_ACTIVATED -> showMessage(getString(R.string.task_marked_active))
            COMPLETE_TASKS_CLEARED -> showMessage(getString(R.string.completed_tasks_cleared))
            null -> {

            }
        }

        if(state.tasks.isEmpty()) {
            when(state.tasksFilterType) {
                TasksFilterType.ACTIVE_TASKS -> showNoActiveTasks()
                TasksFilterType.COMPLETED_TASKS -> showNoCompletedTasks()
                else -> showNoTasks()
            }
        } else {
            listAdapter.replaceData(state.tasks)
            tasksView.visibility = View.VISIBLE
            noTasksView.visibility = View.GONE

            when(state.tasksFilterType) {
                TasksFilterType.ACTIVE_TASKS -> showActiveFilterLabel()
                TasksFilterType.COMPLETED_TASKS -> showCompletedFilterLabel()
                else -> showAllFilterLabel()

            }        }

    }

    private fun showNoActiveTasks() {
        showNoTasksViews(
            resources.getString(R.string.no_tasks_active),
            R.drawable.ic_check_circle_24dp, false
        )
    }

    private fun showNoTasks() {
        showNoTasksViews(
            resources.getString(R.string.no_tasks_all),
            R.drawable.ic_assignment_turned_in_24dp, true
        )
    }

    private fun showNoCompletedTasks() {
        showNoTasksViews(
            resources.getString(R.string.no_tasks_completed),
            R.drawable.ic_verified_user_24dp, false
        )
    }

    private fun showNoTasksViews(
        mainText: String,
        iconRes: Int,
        showAddView: Boolean
    ) {
        tasksView.visibility = View.GONE
        noTasksView.visibility = View.VISIBLE

        noTaskMainView.text = mainText
        noTaskIcon.setImageDrawable(ContextCompat.getDrawable(requireContext(), iconRes))
        noTaskAddView.visibility = if(showAddView) View.VISIBLE else View.GONE
    }

    private fun showActiveFilterLabel() {
        filteringLabelView.text = resources.getString(R.string.label_active)
    }

    private fun showCompletedFilterLabel() {
        filteringLabelView.text = resources.getString(R.string.label_completed)
    }

    private fun showAllFilterLabel() {
        filteringLabelView.text = resources.getString(R.string.label_all)
    }

    private fun showLoadingTasksError() {
        showMessage(getString(R.string.loading_tasks_error))
    }

    companion object {
        operator fun invoke(): TasksFragment = TasksFragment()

    }}