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
        val resolver = pending.activity.contentResolver
        val extras =
            Bundle().apply {
                putString("uri", pending.uri.toString())
                putString("filename", pending.filename)
                putString("file_path", pending.filePath)
                putLong("delay_ms", delayMs)
            }

        shareIoExecutor.execute {
            runCatching {
                val result =
                    resolver.call(
                        Uri.parse("content://$DELETION_PROVIDER_AUTHORITY"),
                        "enqueue",
                        null,
                        extras,
                    )
                val status = result?.getString("status")
                val message = result?.getString("message")
                debugLog { "deletion enqueue status=$status message=$message" }
            }.onFailure { debugLog(it) { "Failed to enqueue deletion" } }
        }
    }
}
