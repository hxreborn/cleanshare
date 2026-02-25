package eu.hxreborn.cleanshare.hook.deletion

import android.app.Activity
import android.content.ContentUris
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.provider.MediaStore
import eu.hxreborn.cleanshare.CleanShareModule
import eu.hxreborn.cleanshare.util.DEFAULT_SCREENSHOT_PATTERN
import eu.hxreborn.cleanshare.util.PREFS_FILE_NAME
import eu.hxreborn.cleanshare.util.PREF_KEY_SCREENSHOT_PATTERN
import eu.hxreborn.cleanshare.util.QUERY_PROVIDER_AUTHORITY
import eu.hxreborn.cleanshare.util.debugLog

internal data class ScreenshotInfo(
    val filename: String,
    val filePath: String?,
    val resolvedUri: Uri? = null,
)

// Typed overload is API 33+
@Suppress("DEPRECATION")
internal inline fun <reified T : Parcelable> Intent.getParcelableExtraCompat(name: String): T? =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getParcelableExtra(name, T::class.java)
    } else {
        getParcelableExtra(name) as? T
    }

internal fun extractShareIntent(rawIntent: Intent): Intent? {
    // The actual ACTION_SEND intent and payload are inside EXTRA_INTENT
    val intent =
        if (rawIntent.action == Intent.ACTION_CHOOSER) {
            rawIntent.getParcelableExtraCompat<Intent>(Intent.EXTRA_INTENT) ?: run {
                debugLog { "EXTRA_INTENT is null" }
                return null
            }
        } else {
            rawIntent
        }

    debugLog { "intent action=${intent.action} type=${intent.type}" }
    if (intent.action != Intent.ACTION_SEND) return null

    val mimeType = intent.type ?: return null
    if (!mimeType.startsWith("image/")) return null

    return intent
}

internal fun extractImageUri(intent: Intent): Uri? {
    val uri =
        intent.getParcelableExtraCompat<Uri>(Intent.EXTRA_STREAM) ?: run {
            debugLog { "EXTRA_STREAM is null" }
            return null
        }
    debugLog { "uri=$uri" }
    return uri
}

// Original media file authorities
private val ORIGINAL_FILE_AUTHORITIES =
    setOf(
        "media",
        "com.android.providers.media",
        "com.android.providers.media.documents",
    )

// SystemUI notification share
private val SCREENSHOT_FILE_PROVIDERS =
    setOf(
        "com.android.systemui.fileprovider",
    )

// Temp/edited file indicators
private val TEMP_PATH_PATTERNS =
    listOf("/cache/", "/temp/", "edited", ".pending")

private fun getScreenshotPattern(): Regex =
    runCatching {
        val pattern =
            CleanShareModule.instance
                ?.getRemotePreferences(PREFS_FILE_NAME)
                ?.getString(PREF_KEY_SCREENSHOT_PATTERN, DEFAULT_SCREENSHOT_PATTERN)
                ?: DEFAULT_SCREENSHOT_PATTERN
        Regex(pattern)
    }.getOrElse { Regex(DEFAULT_SCREENSHOT_PATTERN) }

internal fun isOriginalFileUri(uri: Uri): Boolean {
    val authority = uri.authority ?: return false

    if (authority in SCREENSHOT_FILE_PROVIDERS) return true

    if (authority !in ORIGINAL_FILE_AUTHORITIES) {
        debugLog { "Skipping non-media authority: $authority" }
        return false
    }

    val path = uri.path?.lowercase() ?: return true
    if (TEMP_PATH_PATTERNS.any { path.contains(it) }) {
        debugLog { "Skipping temp/edited path: $path" }
        return false
    }
    return true
}

internal fun getScreenshotInfo(
    activity: Activity,
    uri: Uri,
): ScreenshotInfo? {
    if (!isOriginalFileUri(uri)) return null

    val authority = uri.authority
    if (authority != null && authority in SCREENSHOT_FILE_PROVIDERS) {
        return getScreenshotInfoFromFileProvider(uri)
    }

    runCatching {
        val projection =
            arrayOf(
                MediaStore.Images.Media.RELATIVE_PATH,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.SIZE,
                MediaStore.Images.Media.IS_PENDING,
                MediaStore.Images.Media.IS_TRASHED,
            )
        activity.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val relativePath = cursor.getString(0).orEmpty()
                val name = cursor.getString(1).orEmpty()
                val filePath = cursor.getString(2)
                debugLog {
                    "getScreenshotInfo: relativePath=$relativePath name=$name filePath=$filePath"
                }

                val isPendingIdx = cursor.getColumnIndex(MediaStore.Images.Media.IS_PENDING)
                if (isPendingIdx >= 0 && cursor.getInt(isPendingIdx) == 1) {
                    debugLog { "Skipping pending file: $name" }
                    return null
                }

                val isTrashedIdx = cursor.getColumnIndex(MediaStore.Images.Media.IS_TRASHED)
                if (isTrashedIdx >= 0 && cursor.getInt(isTrashedIdx) == 1) {
                    debugLog { "Skipping trashed file: $name" }
                    return null
                }

                val sizeIdx = cursor.getColumnIndex(MediaStore.Images.Media.SIZE)
                if (sizeIdx >= 0 && cursor.getLong(sizeIdx) == 0L) {
                    debugLog { "Skipping zero-size file: $name" }
                    return null
                }

                val isMatch =
                    relativePath.contains("Screenshots", ignoreCase = true) ||
                        name.matches(getScreenshotPattern())
                if (isMatch) {
                    debugLog { "isScreenshot=true" }
                    return ScreenshotInfo(filename = name, filePath = filePath)
                }
            }
        }
    }.onFailure { debugLog(it) { "getScreenshotInfo query failed" } }

    debugLog { "isScreenshot=false" }
    return null
}

// Cover slow editor flows (crop → annotate → share)
private const val RECENCY_WINDOW_SECONDS = 15 * 60

// Resolve original screenshot via IPC — IntentResolver lacks media permissions
internal fun resolveOriginalScreenshot(activity: Activity): ScreenshotInfo? {
    runCatching {
        val cutoff = System.currentTimeMillis() / 1000 - RECENCY_WINDOW_SECONDS
        val screenshotPattern = getScreenshotPattern()

        val where = "relative_path LIKE '%Screenshots%' AND date_added > $cutoff AND is_trashed = 0"
        val extras = Bundle().apply { putString("where", where) }

        val result =
            activity.contentResolver.call(
                Uri.parse("content://$QUERY_PROVIDER_AUTHORITY"),
                "resolve_screenshot",
                null,
                extras,
            )
        val lines = result?.getStringArray("lines") ?: return null

        for (line in lines) {
            // Row format: "Row: N _id=123, _display_name=foo.png, _data=/path"
            val id =
                ROW_ID_PATTERN
                    .find(line)
                    ?.groupValues
                    ?.get(1)
                    ?.toLongOrNull() ?: continue
            val name =
                ROW_NAME_PATTERN
                    .find(line)
                    ?.groupValues
                    ?.get(1)
                    .orEmpty()
            val filePath = ROW_DATA_PATTERN.find(line)?.groupValues?.get(1)

            if (!name.matches(screenshotPattern)) {
                debugLog { "resolveOriginalScreenshot: candidate name=$name (pattern miss)" }
                continue
            }

            val resolvedUri =
                ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    id,
                )
            debugLog {
                "resolveOriginalScreenshot: matched name=$name filePath=$filePath resolvedUri=$resolvedUri"
            }
            return ScreenshotInfo(filename = name, filePath = filePath, resolvedUri = resolvedUri)
        }
    }.onFailure { debugLog(it) { "resolveOriginalScreenshot failed" } }

    debugLog { "resolveOriginalScreenshot: no recent screenshot found" }
    return null
}

private val ROW_ID_PATTERN = Regex("""_id=(\d+)""")
private val ROW_NAME_PATTERN = Regex("""_display_name=([^,]+)""")
private val ROW_DATA_PATTERN = Regex("""_data=([^,]+)""")

private fun getScreenshotInfoFromFileProvider(uri: Uri): ScreenshotInfo? {
    val filename = uri.lastPathSegment ?: return null
    if (!filename.matches(getScreenshotPattern())) {
        debugLog { "FileProvider file not a screenshot: $filename" }
        return null
    }
    debugLog { "isScreenshot=true (FileProvider)" }
    return ScreenshotInfo(filename = filename, filePath = null)
}
