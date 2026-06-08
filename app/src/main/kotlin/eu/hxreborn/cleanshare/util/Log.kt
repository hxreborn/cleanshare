package eu.hxreborn.cleanshare.util

import android.util.Log
import eu.hxreborn.cleanshare.BuildConfig
import eu.hxreborn.cleanshare.CleanShareModule.Companion.TAG
import eu.hxreborn.cleanshare.module

internal fun log(
    msg: String,
    t: Throwable? = null,
) {
    if (t != null) {
        module.log(Log.ERROR, TAG, msg, t)
    } else {
        module.log(Log.INFO, TAG, msg)
    }
}

internal inline fun debugLog(
    t: Throwable? = null,
    msg: () -> String,
) {
    if (BuildConfig.DEBUG) log(msg(), t)
}
