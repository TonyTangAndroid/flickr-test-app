package com.github.programmerr47.flickrawesomeclient.widgets.paging

import android.content.Context
import androidx.viewpager.widget.ViewPager
import android.util.AttributeSet
import android.view.MotionEvent

class HackyViewPager(context: Context, attributeSet: AttributeSet?) : androidx.viewpager.widget.ViewPager(context, attributeSet) {

    constructor(context: Context) : this(context, null)

    override fun onInterceptTouchEvent(ev: MotionEvent?) =
            try {
                super.onInterceptTouchEvent(ev)
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }

}