package com.github.programmerr47.flickrawesomeclient.widgets.lists

import androidx.annotation.IdRes
import androidx.recyclerview.widget.RecyclerView
import android.view.View

open class BindRecyclerHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
    protected fun <T : View> bind(@IdRes id: Int) = itemView.findViewById<T>(id)
}