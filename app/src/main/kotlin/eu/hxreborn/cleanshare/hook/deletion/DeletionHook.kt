package eu.hxreborn.cleanshare.hook.deletion

import android.net.Uri
import android.os.Bundle
import eu.hxreborn.cleanshare.CleanShareModule
import eu.hxreborn.cleanshare.util.DEFAULT_DELETION_DELAY_MS
import eu.hxreborn.cleanshare.util.DELETION_PROVIDER_AUTHORITY
import eu.hxreborn.cleanshare.util.PREFS_FILE_NAME
import eu.hxreborn.cleanshare.util.PREF_KEY_DELETION_DELAY_MS
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

            debugLog { "Enqueuing deletion filename=${pending.filename}" }
            runCatching {
                val delayMs =
                    CleanShareModule.instance
                        ?.getRemotePreferences(PREFS_FILE_NAME)
                        ?.getInt(PREF_KEY_DELETION_DELAY_MS, DEFAULT_DELETION_DELAY_MS)
                        ?.toLong()
                        ?: DEFAULT_DELETION_DELAY_MS.toLong()

                val extras =
                    Bundle().apply {
                        putString("uri", pending.uri.toString())
                        putString("filename", pending.filename)
                        putString("file_path", pending.filePath)
                        putLong("delay_ms", delayMs)
                    }

                pending.activity.contentResolver.call(
                    Uri.parse("content://$DELETION_PROVIDER_AUTHORITY"),
                    "enqueue",
                    null,
                    extras,
                )
                debugLog { "ContentProvider enqueue called" }
            }.onFailure { debugLog(it) { "Failed to enqueue deletion" } }
        }
    }
}
