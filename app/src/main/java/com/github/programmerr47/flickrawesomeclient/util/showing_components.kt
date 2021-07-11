package com.github.programmerr47.flickrawesomeclient.util

import android.app.Activity
import android.content.Context
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import android.view.View

fun androidx.fragment.app.FragmentManager.commitTransaction(block: androidx.fragment.app.FragmentTransaction.() -> Unit) =
        beginTransaction().apply(block).commit()

fun androidx.fragment.app.Fragment.hideKeyboard() = activity?.hideKeyboard()

fun Activity.hideKeyboard() {
    currentFocus?.run {
        inputMethodManager.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
        currentFocus?.clearFocus()
    }
}

fun Activity.finishNoAnim() {
    finish()
    overridePendingTransition(0, 0)
}

fun <T : View> Activity.bindable(@IdRes id: Int) = lazy { findViewById<T>(id) }