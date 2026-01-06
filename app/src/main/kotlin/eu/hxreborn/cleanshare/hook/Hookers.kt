package eu.hxreborn.cleanshare.hook

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
            callback.returnAndSkip(true)
        }
    }
}

@XposedHooker
class ShareTargetsHooker : Hooker {
    companion object {
        @JvmStatic
        @BeforeInvocation
        fun before(callback: BeforeHookCallback) {
            callback.returnAndSkip(emptyList<Any>())
        }
    }
}
