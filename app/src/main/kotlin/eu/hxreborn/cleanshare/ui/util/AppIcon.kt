package eu.hxreborn.cleanshare.ui.util

import android.content.Context
import android.content.pm.LauncherApps
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.UserHandle
import android.util.LruCache
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.core.graphics.createBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val MAX_ICON_PX = 512

private val iconCache = LruCache<String, ImageBitmap>(64)

internal fun Drawable.toIconBitmap(size: Int): Bitmap {
    val side = size.coerceIn(1, MAX_ICON_PX)
    val bitmap = createBitmap(side, side)
    val previousBounds = Rect(bounds)
    setBounds(0, 0, side, side)
    draw(Canvas(bitmap))
    bounds = previousBounds
    return bitmap
}

@Composable
internal fun rememberAppIcon(
    packageName: String,
    sizePx: Int,
    user: UserHandle? = null,
): ImageBitmap? {
    val context = LocalContext.current
    val key = "$packageName:$sizePx:$user"
    return produceState<ImageBitmap?>(initialValue = iconCache.get(key), key1 = key) {
        if (value != null) return@produceState
        value =
            withContext(Dispatchers.IO) {
                runCatching {
                    loadIcon(context, packageName, user)
                        .toIconBitmap(sizePx)
                        .asImageBitmap()
                }.getOrNull()?.also { iconCache.put(key, it) }
            }
    }.value
}

private fun loadIcon(
    context: Context,
    packageName: String,
    user: UserHandle?,
): Drawable =
    if (user == null) {
        context.packageManager.getApplicationIcon(packageName)
    } else {
        context
            .getSystemService(LauncherApps::class.java)
            .getActivityList(packageName, user)
            .first()
            .getBadgedIcon(0)
    }
