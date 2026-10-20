package eu.hxreborn.cleanshare.ui.util

import android.content.res.AssetManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val NOTES_FILE = Regex("""(\d+)\.txt""")

@Immutable
data class Release(
    val versionCode: Int,
    val versionName: String,
    val notes: List<String>,
)

fun loadReleases(assets: AssetManager): List<Release> =
    assets
        .list("")
        .orEmpty()
        .mapNotNull {
            NOTES_FILE
                .matchEntire(it)
                ?.groupValues
                ?.get(1)
                ?.toIntOrNull()
        }.sortedDescending()
        .map { code ->
            Release(
                versionCode = code,
                versionName = code.toString().toList().joinToString("."),
                notes = parseNotes(assets.open("$code.txt").bufferedReader().use { it.readText() }),
            )
        }

fun parseNotes(text: String): List<String> =
    text
        .lines()
        .map { it.trim().removePrefix("- ").trim() }
        .filter { it.isNotEmpty() }

@Composable
fun rememberReleases(): State<List<Release>?> {
    val assets = LocalContext.current.assets
    return produceState<List<Release>?>(null, assets) {
        value = withContext(Dispatchers.IO) { loadReleases(assets) }
    }
}
