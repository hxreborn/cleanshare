package eu.hxreborn.cleanshare.deletion

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import eu.hxreborn.cleanshare.util.PREFS_FILE_NAME
import eu.hxreborn.cleanshare.util.RootUtils

class ScreenshotQueryProvider : ContentProvider() {
    override fun onCreate(): Boolean = true

    override fun call(
        method: String,
        arg: String?,
        extras: Bundle?,
    ): Bundle {
        if (!isCallerAllowed()) {
            return errorResult("unauthorized")
        }
        return when (method) {
            "resolve_screenshot" -> handleResolveScreenshot(extras)
            "prefs" -> handlePrefs()
            else -> errorResult("unknown method: $method")
        }
    }

    private fun handleResolveScreenshot(extras: Bundle?): Bundle {
        val whereSql =
            extras?.getString("where")
                ?: return errorResult("missing where")
        val lines = RootUtils.queryRecentScreenshots(whereSql)
        return okResult { putStringArray("lines", lines.toTypedArray()) }
    }

    private fun handlePrefs(): Bundle {
        val prefs =
            context?.getSharedPreferences(PREFS_FILE_NAME, Context.MODE_PRIVATE)
                ?: return errorResult("no context")
        return okResult {
            prefs.all.forEach { (key, value) ->
                when (value) {
                    is Boolean -> putBoolean(key, value)
                    is Int -> putInt(key, value)
                    is String -> putString(key, value)
                    is Set<*> -> putStringArrayList(key, value.mapTo(ArrayList()) { it.toString() })
                }
            }
        }
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
