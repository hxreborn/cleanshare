package eu.hxreborn.cleanshare.hook.deletion

import android.net.Uri
import android.os.Bundle
import eu.hxreborn.cleanshare.util.DELETION_PROVIDER_AUTHORITY
import eu.hxreborn.cleanshare.util.debugLog

internal object DeletionHook {
    fun onShareStarted(delayMs: Long) {
        debugLog { "startSelected hook fired" }

        val pending = ShareState.consumeIfPendingDeletion() ?: return

        debugLog { "Enqueuing deletion filename=${pending.filename}" }
        runCatching {
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
