package eu.hxreborn.cleanshare.deletion

import android.content.Context
import android.content.SharedPreferences
import android.os.Handler
import android.os.Looper
import android.util.Log
import eu.hxreborn.cleanshare.prefs.DeletionAction
import eu.hxreborn.cleanshare.util.PREFS_FILE_NAME
import eu.hxreborn.cleanshare.util.PREF_KEY_DELETION_ACTION
import eu.hxreborn.cleanshare.util.PREF_KEY_SHOW_DELETION_TOAST
import eu.hxreborn.cleanshare.util.RootUtils

internal class DeletionExecutor(
    private val context: Context,
    private val queue: DeletionQueue,
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_FILE_NAME, Context.MODE_PRIVATE)
    private val handler = Handler(Looper.getMainLooper())

    fun schedule(request: DeletionRequest) {
        val delay = (request.scheduledAt - System.currentTimeMillis()).coerceAtLeast(0L)
        handler.postDelayed({ execute(request) }, delay)
    }

    private fun execute(request: DeletionRequest) {
        queue.updateStatus(request.id, RequestStatus.EXECUTING)
        Log.d(TAG, "Executing deletion: ${request.id}")

        if (!RootUtils.isRootAvailable()) {
            Log.d(TAG, "Deletion failed: root not available for ${request.id}")
            queue.updateStatus(request.id, RequestStatus.FAILED)
            showToast("Failed to delete: ${request.filename}")
            return
        }

        val actionKey =
            prefs.getString(PREF_KEY_DELETION_ACTION, DeletionAction.DELETE.key)
                ?: DeletionAction.DELETE.key
        val action = DeletionAction.fromKey(actionKey)

        when (action) {
            DeletionAction.TRASH -> {
                executeTrash(request)
            }

            DeletionAction.DELETE -> {
                val filePath = request.filePath
                if (filePath == null) {
                    Log.d(TAG, "Deletion failed: no file path for ${request.id}")
                    queue.updateStatus(request.id, RequestStatus.FAILED)
                    showToast("Failed to delete: ${request.filename}")
                    return
                }
                executeDelete(request, filePath)
            }
        }
    }

    private fun executeTrash(request: DeletionRequest) {
        val trashed = RootUtils.trashMediaStoreEntry(request.uri)
        if (trashed) {
            queue.updateStatus(request.id, RequestStatus.COMPLETED)
            showToast("Trashed: ${request.filename}")
        } else {
            Log.d(TAG, "Trash failed for ${request.id}")
            queue.updateStatus(request.id, RequestStatus.FAILED)
            showToast("Failed to trash: ${request.filename}")
        }
    }

    private fun executeDelete(
        request: DeletionRequest,
        filePath: String,
    ) {
        // Delete MediaStore entry first (via root), then physical file
        // This prevents Google Photos from showing black tiles
        val mediaStoreDeleted = RootUtils.deleteMediaStoreEntry(request.uri)
        Log.d(TAG, "MediaStore delete: $mediaStoreDeleted for ${request.id}")

        val fileDeleted = RootUtils.deleteFile(filePath)
        Log.d(TAG, "File delete: $fileDeleted for ${request.id}")

        if (mediaStoreDeleted || fileDeleted) {
            Log.d(TAG, "Deletion success: ${request.id}")
            queue.updateStatus(request.id, RequestStatus.COMPLETED)
            showToast("Deleted: ${request.filename}")
        } else {
            Log.d(TAG, "Deletion failed: both MediaStore and file delete failed for ${request.id}")
            queue.updateStatus(request.id, RequestStatus.FAILED)
            showToast("Failed to delete: ${request.filename}")
        }
    }

    private fun showToast(message: String) {
        if (!prefs.getBoolean(PREF_KEY_SHOW_DELETION_TOAST, true)) return
        RootUtils.showLocalToast(context, message)
    }

    companion object {
        private const val TAG = "CleanShare"
    }
}
