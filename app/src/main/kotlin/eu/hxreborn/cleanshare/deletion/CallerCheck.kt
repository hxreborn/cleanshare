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
    val myUserBase = Process.myUid() / PER_USER_RANGE * PER_USER_RANGE
    val callerUidInMyUser = myUserBase + callerUid % PER_USER_RANGE
    val callerPackages =
        context?.packageManager?.getPackagesForUid(callerUidInMyUser)
            ?: return false
    return callerPackages.any { it in ALLOWED_CALLER_PACKAGES }
}
