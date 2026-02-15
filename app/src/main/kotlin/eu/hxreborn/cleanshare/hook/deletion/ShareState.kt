package eu.hxreborn.cleanshare.hook.deletion

import android.app.Activity
import android.net.Uri
import java.lang.ref.WeakReference
import java.util.concurrent.atomic.AtomicReference

data class PendingDeletion(
    val uri: Uri,
    val filename: String,
    val activity: Activity,
)

data class ShareSession(
    val uri: Uri,
    val filename: String,
    val shouldDelete: Boolean,
    val activityRef: WeakReference<Activity>,
)

object ShareState {
    private val current = AtomicReference<ShareSession?>(null)

    fun set(
        uri: Uri,
        filename: String,
        shouldDelete: Boolean,
        activity: Activity,
    ) {
        current.set(ShareSession(uri, filename, shouldDelete, WeakReference(activity)))
    }

    fun updateShouldDelete(value: Boolean) {
        current.getAndUpdate { session ->
            session?.copy(shouldDelete = value)
        }
    }

    fun consumeIfPendingDeletion(): PendingDeletion? {
        val session = current.getAndSet(null) ?: return null
        if (!session.shouldDelete) return null
        val activity = session.activityRef.get() ?: return null
        return PendingDeletion(session.uri, session.filename, activity)
    }
}
