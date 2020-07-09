package com.benmohammad.mvitodos.addedittask

import android.os.Bundle
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import com.benmohammad.mvitodos.R
import com.benmohammad.mvitodos.util.addFragmentToActivity

class AddEditTaskActivity: AppCompatActivity() {

    private lateinit var actionBar: ActionBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_task_act)

        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.run {
            actionBar = this
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }

        val taskId = intent.getStringExtra(AddEditTaskFragment.ARGUMENT_EDIT_TASK_ID)
        setToolBarTitle(taskId)

        if(supportFragmentManager.findFragmentById(R.id.contentFrame) == null) {
            val addEditTaskFragment = AddEditTaskFragment.invoke()
            if(taskId == null) {
                val args = Bundle()
                args.putString(AddEditTaskFragment.ARGUMENT_EDIT_TASK_ID, taskId)
                addEditTaskFragment.arguments = args
            }

            addFragmentToActivity(supportFragmentManager, addEditTaskFragment, R.id.contentFrame)

        }
    }

    private fun setToolBarTitle(taskId: String?) {
        actionBar.setTitle(if(taskId == null) R.string.add_task else R.string.edit_task)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
    companion object {
        const val REQUEST_ADD_TASK = 1

    }
}