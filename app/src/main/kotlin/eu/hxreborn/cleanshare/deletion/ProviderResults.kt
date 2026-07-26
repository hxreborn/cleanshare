package eu.hxreborn.cleanshare.deletion

import android.os.Bundle

internal fun errorResult(message: String): Bundle =
    Bundle().apply {
        putString("status", "error")
        putString("message", message)
    }

internal inline fun okResult(build: Bundle.() -> Unit = {}): Bundle =
    Bundle().apply {
        putString("status", "ok")
        build()
    }
