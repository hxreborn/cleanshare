package eu.hxreborn.cleanshare.hook.deletion

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Parcelable
import android.provider.MediaStore
import eu.hxreborn.cleanshare.util.SCREENSHOT_NAME_PATTERN
import eu.hxreborn.cleanshare.util.debugLog

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

internal fun getFilenameIfScreenshot(
    activity: Activity,
    uri: Uri,
): String? {
    runCatching {
        val projection =
            arrayOf(
                MediaStore.Images.Media.RELATIVE_PATH,
                MediaStore.Images.Media.DISPLAY_NAME,
            )
        activity.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val path = cursor.getString(0).orEmpty()
                val name = cursor.getString(1).orEmpty()
                debugLog { "getFilenameIfScreenshot: path=$path name=$name" }

                val isMatch =
                    path.contains("Screenshots", ignoreCase = true) ||
                        name.matches(
                            SCREENSHOT_NAME_PATTERN,
                        )
                if (isMatch) {
                    debugLog { "isScreenshot=true" }
                    return name
                }
            }
        }
    }.onFailure { debugLog(it) { "getFilenameIfScreenshot query failed" } }

    debugLog { "isScreenshot=false" }
    return null
}
