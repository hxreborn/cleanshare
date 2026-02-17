package eu.hxreborn.cleanshare.hook.directshare

import eu.hxreborn.cleanshare.CleanShareModule
import eu.hxreborn.cleanshare.util.PREFS_FILE_NAME
import eu.hxreborn.cleanshare.util.PREF_KEY_HIDE_DIRECT_SHARE
import io.github.libxposed.api.XposedInterface.BeforeHookCallback
import io.github.libxposed.api.XposedInterface.Hooker
import io.github.libxposed.api.annotations.BeforeInvocation
import io.github.libxposed.api.annotations.XposedHooker

@XposedHooker
class LowRamHooker : Hooker {
    companion object {
        @JvmStatic
        @BeforeInvocation
        fun before(callback: BeforeHookCallback) {
            val module = CleanShareModule.instance ?: return
            val prefs = module.getRemotePreferences(PREFS_FILE_NAME)
            val enabled = prefs.getBoolean(PREF_KEY_HIDE_DIRECT_SHARE, true)
            if (enabled) {
                callback.returnAndSkip(true)
            }
        }
    }
}

@XposedHooker
class ShareTargetsHooker : Hooker {
    companion object {
        @JvmStatic
        @BeforeInvocation
        fun before(callback: BeforeHookCallback) {
            val module = CleanShareModule.instance ?: return
            val prefs = module.getRemotePreferences(PREFS_FILE_NAME)
            val enabled = prefs.getBoolean(PREF_KEY_HIDE_DIRECT_SHARE, true)
            if (enabled) {
                callback.returnAndSkip(emptyList<Any>())
            }
        }
    }
}
