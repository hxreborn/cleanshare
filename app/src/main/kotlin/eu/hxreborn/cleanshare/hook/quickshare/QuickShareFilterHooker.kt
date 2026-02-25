package eu.hxreborn.cleanshare.hook.quickshare

import android.content.pm.ResolveInfo
import eu.hxreborn.cleanshare.CleanShareModule
import eu.hxreborn.cleanshare.util.PREFS_FILE_NAME
import eu.hxreborn.cleanshare.util.PREF_KEY_HIDE_QUICK_SHARE
import eu.hxreborn.cleanshare.util.QUICK_SHARE_ACTIVITY
import eu.hxreborn.cleanshare.util.debugLog
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

            debugLog { "[QuickShare] queryIntentActivitiesAsUser called" }

            val prefs = module.getRemotePreferences(PREFS_FILE_NAME)
            val enabled = prefs?.getBoolean(PREF_KEY_HIDE_QUICK_SHARE, false) ?: false
            if (!enabled) return

            val result = callback.result

            @Suppress("UNCHECKED_CAST")
            val list = result as? MutableList<ResolveInfo>
            if (list == null) {
                debugLog { "[QuickShare] result is not MutableList<ResolveInfo>" }
                return
            }

            debugLog { "[QuickShare] list size before filter: ${list.size}" }

            val removed = list.removeAll { it.activityInfo?.name == QUICK_SHARE_ACTIVITY }
            debugLog { "[QuickShare] removed=$removed, list size after: ${list.size}" }
        }
    }
}
