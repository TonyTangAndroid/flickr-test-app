package com.github.programmerr47.flickrawesomeclient.util

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.support.annotation.DrawableRes
import android.support.v7.content.res.AppCompatResources
import android.view.inputmethod.InputMethodManager
import kotlin.reflect.KClass

fun isNetworkAvailable(context: Context): Boolean {
    val activeNetworkInfo = context.connectivityManager.activeNetworkInfo
    return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting
}

fun Context.getDrawableCompat(@DrawableRes drawableRes: Int) = AppCompatResources.getDrawable(this, drawableRes)

fun Context.startActivity(clazz: KClass<*>, intentFun: Intent.() -> Unit) =
        startActivity(Intent(this, clazz.java).apply(intentFun))

val Context.inputMethodManager
    get() = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

val Context.connectivityManager
    get() = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager