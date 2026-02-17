package eu.hxreborn.cleanshare.deletion

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.os.Binder
import android.os.Bundle
import android.util.Log
import androidx.core.os.bundleOf
import eu.hxreborn.cleanshare.util.RootUtils

class DeletionProvider : ContentProvider() {
    companion object {
        const val AUTHORITY = "eu.hxreborn.cleanshare.deletion"
        private const val TAG = "CleanShare"

        private val ALLOWED_CALLER_PACKAGES =
            setOf(
                "com.android.intentresolver",
                "android",
                "com.android.systemui",
            )

        private val ALLOWED_URI_AUTHORITIES =
            setOf(
                "media",
                "com.android.providers.media",
                "com.android.providers.media.documents",
            )
    }

    private lateinit var queue: DeletionQueue
    private lateinit var executor: DeletionExecutor

    override fun onCreate(): Boolean {
        val ctx = context ?: return false
        queue = DeletionQueue(ctx.filesDir)
        executor = DeletionExecutor(ctx, queue)
        RootUtils.grantPostNotificationsPermission(ctx.packageName)
        reschedulePendingRequests()
        return true
    }

    private fun reschedulePendingRequests() {
        val pending = queue.getPendingRequests()
        if (pending.isEmpty()) return
        Log.d(TAG, "Rescheduling ${pending.size} pending deletions")
        pending.forEach { executor.schedule(it) }
    }

    override fun call(
        method: String,
        arg: String?,
        extras: Bundle?,
    ): Bundle {
        if (!verifyCallerAllowed()) {
            Log.d(TAG, "Caller rejected: uid=${Binder.getCallingUid()}")
            return bundleOf("status" to "error", "message" to "unauthorized")
        }
        return when (method) {
            "enqueue" -> handleEnqueue(extras)
            "cancel" -> handleCancel(extras)
            else -> bundleOf("status" to "error", "message" to "unknown method: $method")
        }
    }

    private fun handleEnqueue(extras: Bundle?): Bundle {
        val uriString =
            extras?.getString("uri")
                ?: return bundleOf("status" to "error", "message" to "missing uri")

        val uri =
            runCatching { Uri.parse(uriString) }.getOrNull()
                ?: return bundleOf("status" to "error", "message" to "invalid uri")

        if (!isValidDeletionUri(uri)) {
            Log.d(TAG, "Validation failed: invalid uri")
            return bundleOf("status" to "error", "message" to "invalid uri")
        }

        val displayFilename = extras.getString("filename") ?: uri.lastPathSegment ?: "file"
        val filePath = extras.getString("file_path")
        val delayMs = extras.getLong("delay_ms", 10_000L).coerceIn(0L, 60_000L)
        val now = System.currentTimeMillis()

        val request =
            DeletionRequest(
                id =
                    java.util.UUID
                        .randomUUID()
                        .toString(),
                uri = uriString,
                filePath = filePath,
                filename = displayFilename,
                createdAt = now,
                scheduledAt = now + delayMs,
                status = RequestStatus.PENDING,
            )

        queue.enqueue(request)
        executor.schedule(request)
        Log.d(TAG, "Enqueued deletion: ${request.id}")
        return bundleOf("status" to "ok", "request_id" to request.id)
    }

    private fun handleCancel(extras: Bundle?): Bundle {
        val uriString =
            extras?.getString("uri")
                ?: return bundleOf("status" to "error", "message" to "missing uri")
        val canceled = queue.cancel(uriString)
        return bundleOf("status" to if (canceled) "ok" else "not_found")
    }

    private fun verifyCallerAllowed(): Boolean {
        val callerUid = Binder.getCallingUid()
        if (callerUid == android.os.Process.myUid()) return true
        val callerPackages =
            context?.packageManager?.getPackagesForUid(callerUid)
                ?: return false
        return callerPackages.any { it in ALLOWED_CALLER_PACKAGES }
    }

    private fun isValidDeletionUri(uri: Uri): Boolean {
        if (uri.scheme != "content") return false
        val authority = uri.authority ?: return false
        return authority in ALLOWED_URI_AUTHORITIES
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?,
    ): Cursor? = null

    override fun getType(uri: Uri): String? = null

    override fun insert(
        uri: Uri,
        values: ContentValues?,
    ): Uri? = null

    override fun delete(
        uri: Uri,
        selection: String?,
        selectionArgs: Array<out String>?,
    ): Int = 0

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?,
    ): Int = 0
}
