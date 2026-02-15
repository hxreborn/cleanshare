package eu.hxreborn.cleanshare.hook.deletion

import android.content.Intent
import eu.hxreborn.cleanshare.util.ACTION_DELETE_SCREENSHOT
import eu.hxreborn.cleanshare.util.DELETE_DELAY_MS
import eu.hxreborn.cleanshare.util.debugLog
import io.github.libxposed.api.XposedInterface.AfterHookCallback
import io.github.libxposed.api.XposedInterface.Hooker
import io.github.libxposed.api.annotations.AfterInvocation
import io.github.libxposed.api.annotations.XposedHooker

@XposedHooker
class DeletionHook : Hooker {
    companion object {
        @JvmStatic
        @AfterInvocation
        fun after(callback: AfterHookCallback) {
            debugLog { "startSelected hook fired" }

            val pending = ShareState.consumeIfPendingDeletion() ?: return

            debugLog { "Sending deletion broadcast file=${pending.filename}" }
            runCatching {
                val intent =
                    Intent(ACTION_DELETE_SCREENSHOT).apply {
                        putExtra("uri", pending.uri.toString())
                        putExtra("filename", pending.filename)
                        putExtra("delay_ms", DELETE_DELAY_MS)
                    }
                pending.activity.sendBroadcast(intent)
                debugLog { "Broadcast sent; if no deletion, verify 'system' scope and reboot" }
            }.onFailure { debugLog(it) { "Failed to send deletion broadcast" } }
        }
    }
}
