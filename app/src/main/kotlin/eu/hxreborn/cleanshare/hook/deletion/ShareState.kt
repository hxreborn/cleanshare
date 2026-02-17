package eu.hxreborn.cleanshare.hook.deletion

import android.app.Activity
import android.net.Uri
import java.lang.ref.WeakReference
import java.util.concurrent.atomic.AtomicReference

data class PendingDeletion(
    val uri: Uri,
    val filename: String,
    val filePath: String?,
    val activity: Activity,
)

data class ShareSession(
    val uri: Uri,
    val filename: String,
    val filePath: String?,
    val shouldDelete: Boolean,
    val activityRef: WeakReference<Activity>,
)

object ShareState {
    private val currentSession = AtomicReference<ShareSession?>(null)

    fun set(
        uri: Uri,
        filename: String,
        filePath: String?,
        shouldDelete: Boolean,
        activity: Activity,
    ) {
        currentSession.set(
            ShareSession(
                uri = uri,
                filename = filename,
                filePath = filePath,
                shouldDelete = shouldDelete,
                activityRef = WeakReference(activity),
            ),
        )
    }

    fun updateShouldDelete(value: Boolean) {
        currentSession.getAndUpdate { session ->
            session?.copy(shouldDelete = value)
        }
    }

    fun consumeIfPendingDeletion(): PendingDeletion? {
        val session = currentSession.getAndSet(null) ?: return null
        if (!session.shouldDelete) return null
        val activity = session.activityRef.get() ?: return null
        return PendingDeletion(session.uri, session.filename, session.filePath, activity)
    }
}
