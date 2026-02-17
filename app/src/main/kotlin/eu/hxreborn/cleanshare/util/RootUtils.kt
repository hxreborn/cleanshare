package eu.hxreborn.cleanshare.util

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import com.topjohnwu.superuser.Shell
import com.topjohnwu.superuser.io.SuFile
import java.io.File

object RootUtils {
    private const val TAG = "CleanShare"
    private val handler = Handler(Looper.getMainLooper())

    private val ALLOWED_DIRS =
        listOf(
            "/storage/emulated/0/Pictures/Screenshots/",
            "/storage/emulated/0/DCIM/Screenshots/",
            "/data/media/0/Pictures/Screenshots/",
            "/data/media/0/DCIM/Screenshots/",
        )

    fun isRootAvailable(): Boolean =
        runCatching {
            Shell.getShell().isRoot
        }.getOrElse {
            Log.w(TAG, "Root check failed: ${it.message}")
            false
        }

    fun deleteFile(filePath: String): Boolean {
        val canonical =
            runCatching { File(filePath).canonicalPath }.getOrElse {
                Log.w(TAG, "Path canonicalization failed: $filePath")
                return false
            }
        if (ALLOWED_DIRS.none { canonical.startsWith(it) }) {
            Log.w(TAG, "Path not in allowlist: $canonical")
            return false
        }
        return SuFile(canonical).delete().also { ok ->
            if (!ok) Log.w(TAG, "Root delete failed: $canonical")
        }
    }

    // Run content delete as shell user (UID 2000), not root (UID 0)
    // MediaProvider rejects UID 0 with "no associated package"
    fun deleteMediaStoreEntry(uri: String): Boolean {
        if (!isRootAvailable()) return false
        return runCatching {
            Shell.cmd("su 2000 -c \"content delete --uri '$uri'\"").exec().isSuccess
        }.getOrElse {
            Log.w(TAG, "MediaStore delete failed: ${it.message}")
            false
        }
    }

    fun grantPostNotificationsPermission(packageName: String): Boolean {
        if (!isRootAvailable()) return false
        return runCatching {
            Shell
                .cmd(
                    "pm grant $packageName android.permission.POST_NOTIFICATIONS",
                ).exec()
                .isSuccess
        }.getOrElse {
            Log.w(TAG, "Failed to grant POST_NOTIFICATIONS: ${it.message}")
            false
        }
    }

    fun showLocalToast(
        context: Context,
        message: String,
        long: Boolean = false,
    ) {
        handler.post {
            val duration = if (long) Toast.LENGTH_LONG else Toast.LENGTH_SHORT
            Toast.makeText(context, message, duration).show()
        }
    }

    private fun shellQuote(raw: String): String = "'${raw.replace("'", "'\\''")}'"
}
