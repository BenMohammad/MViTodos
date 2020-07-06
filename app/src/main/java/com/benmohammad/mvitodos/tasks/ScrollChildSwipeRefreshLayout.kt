package com.benmohammad.mvitodos.tasks

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

class ScrollChildSwipeRefreshLayout: SwipeRefreshLayout {

    private var scrollUpChild: View? = null

    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet): super(context, attrs)

    override fun canChildScrollUp(): Boolean {
        return scrollUpChild?.canScrollVertically(-1) ?:super.canChildScrollUp()
    }

    fun setScrollChild(view: View) {
        scrollUpChild = view
    }
}