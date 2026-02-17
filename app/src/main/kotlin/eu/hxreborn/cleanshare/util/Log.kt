package eu.hxreborn.cleanshare.util

import eu.hxreborn.cleanshare.BuildConfig
import eu.hxreborn.cleanshare.CleanShareModule

internal fun log(
    msg: String,
    t: Throwable? = null,
) {
    val module = CleanShareModule.instance ?: return
    t?.let { module.log(msg, it) } ?: module.log(msg)
}

internal inline fun debugLog(
    t: Throwable? = null,
    msg: () -> String,
) {
    if (BuildConfig.DEBUG) log(msg(), t)
}
