package com.benmohammad.mvitodos.taskdetail

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.benmohammad.mvitodos.R

class TaskDetailActivity: AppCompatActivity() {

    companion object {
        const val EXTRA_TASK_ID = "TASK_ID"

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.taskdetail_act)
    }
}