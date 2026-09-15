package eu.hxreborn.cleanshare.deletion

import android.content.ContentProvider
import android.os.Binder
import android.os.Process
import eu.hxreborn.cleanshare.util.PER_USER_RANGE

private val ALLOWED_CALLER_PACKAGES =
    setOf(
        "com.android.intentresolver",
        "android",
        "com.android.systemui",
    )

internal fun ContentProvider.isCallerAllowed(): Boolean {
    val callerUid = Binder.getCallingUid()
    if (callerUid == Process.myUid()) return true
    val callerPackages =
        context?.packageManager?.getPackagesForUid(callerUid % PER_USER_RANGE)
            ?: return false
    return callerPackages.any { it in ALLOWED_CALLER_PACKAGES }
}
