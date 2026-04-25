package eu.hxreborn.cleanshare.util

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager

internal fun isLauncherIconVisible(context: Context): Boolean {
    val info =
        context.packageManager.getPackageInfo(
            context.packageName,
            PackageManager.GET_ACTIVITIES,
        )
    return info.activities?.any { it.targetActivity != null } == true
}

internal fun setLauncherIconVisible(
    context: Context,
    visible: Boolean,
) {
    context.packageManager.setComponentEnabledSetting(
        ComponentName(context.packageName, "${context.packageName}.LauncherAlias"),
        if (visible) {
            PackageManager.COMPONENT_ENABLED_STATE_DEFAULT
        } else {
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED
        },
        PackageManager.DONT_KILL_APP,
    )
}
