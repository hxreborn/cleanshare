package eu.hxreborn.cleanshare.deletion

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.os.Binder
import android.os.Bundle
import androidx.core.os.bundleOf
import eu.hxreborn.cleanshare.util.RootUtils

class ScreenshotQueryProvider : ContentProvider() {
    companion object {
        private val ALLOWED_CALLER_PACKAGES =
            setOf(
                "com.android.intentresolver",
                "android",
                "com.android.systemui",
            )
    }

    override fun onCreate(): Boolean = true

    override fun call(
        method: String,
        arg: String?,
        extras: Bundle?,
    ): Bundle {
        if (!verifyCallerAllowed()) {
            return bundleOf("status" to "error", "message" to "unauthorized")
        }
        return when (method) {
            "resolve_screenshot" -> handleResolveScreenshot(extras)
            else -> bundleOf("status" to "error", "message" to "unknown method: $method")
        }
    }

    private fun handleResolveScreenshot(extras: Bundle?): Bundle {
        val whereSql =
            extras?.getString("where")
                ?: return bundleOf("status" to "error", "message" to "missing where")
        val lines = RootUtils.queryRecentScreenshots(whereSql)
        return bundleOf(
            "status" to "ok",
            "lines" to lines.toTypedArray(),
        )
    }

    private fun verifyCallerAllowed(): Boolean {
        val callerUid = Binder.getCallingUid()
        if (callerUid == android.os.Process.myUid()) return true
        val callerPackages =
            context?.packageManager?.getPackagesForUid(callerUid)
                ?: return false
        return callerPackages.any { it in ALLOWED_CALLER_PACKAGES }
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
