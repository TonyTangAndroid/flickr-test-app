package com.github.programmerr47.flickrawesomeclient.util

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

fun DiffUtil.Callback.calculateDiff() = DiffUtil.calculateDiff(this)

fun <VH : androidx.recyclerview.widget.RecyclerView.ViewHolder> androidx.recyclerview.widget.RecyclerView.Adapter<VH>.dispatchUpdatesFrom(diffResult: DiffUtil.DiffResult) =
        diffResult.dispatchUpdatesTo(this)