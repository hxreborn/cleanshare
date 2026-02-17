// TODO: After A11-12 testing, clean up:
//  - Remove verbose logging (module.log calls)
//  - Bump version to 2.0.0 release
//  - Update versionCode to 200

package eu.hxreborn.cleanshare.hook.quickshare

import android.content.pm.ResolveInfo
import eu.hxreborn.cleanshare.CleanShareModule
import eu.hxreborn.cleanshare.util.PREFS_FILE_NAME
import eu.hxreborn.cleanshare.util.PREF_KEY_HIDE_QUICK_SHARE
import eu.hxreborn.cleanshare.util.QUICK_SHARE_ACTIVITY
import io.github.libxposed.api.XposedInterface.AfterHookCallback
import io.github.libxposed.api.XposedInterface.Hooker
import io.github.libxposed.api.annotations.AfterInvocation
import io.github.libxposed.api.annotations.XposedHooker

@XposedHooker
class QuickShareFilterHooker : Hooker {
    companion object {
        @JvmStatic
        @AfterInvocation
        fun after(callback: AfterHookCallback) {
            val module = CleanShareModule.instance ?: return

            module.log("[QuickShare] queryIntentActivitiesAsUser called")

            val prefs = module.getRemotePreferences(PREFS_FILE_NAME)
            val enabled = prefs?.getBoolean(PREF_KEY_HIDE_QUICK_SHARE, false) ?: false
            module.log("[QuickShare] pref enabled=$enabled")

            if (!enabled) return

            val result = callback.result
            module.log("[QuickShare] result type: ${result?.javaClass?.name}")

            @Suppress("UNCHECKED_CAST")
            val list = result as? MutableList<ResolveInfo>
            if (list == null) {
                module.log("[QuickShare] result is not MutableList<ResolveInfo>")
                return
            }

            module.log("[QuickShare] list size before filter: ${list.size}")

            // Log all GMS activities
            list.filter { it.activityInfo?.packageName == "com.google.android.gms" }
                .forEach { info ->
                    module.log("[QuickShare] GMS activity: ${info.activityInfo?.name}")
                }

            val removed = list.removeAll { it.activityInfo?.name == QUICK_SHARE_ACTIVITY }
            module.log("[QuickShare] removed=$removed, list size after: ${list.size}")
        }
    }
}
