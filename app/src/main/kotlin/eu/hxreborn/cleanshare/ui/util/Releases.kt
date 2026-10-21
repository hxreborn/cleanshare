package eu.hxreborn.cleanshare.ui.util

import android.content.res.AssetManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate

private val NOTES_FILE = Regex("""(\d+)\.txt""")
private val BULLET = Regex("""^[-*•]\s+""")
private const val RELEASE_DATES = "release-dates.txt"

@Immutable
data class Release(
    val versionCode: Int,
    val versionName: String,
    val notes: List<String>,
    val releasedOn: LocalDate? = null,
)

fun loadReleases(assets: AssetManager): List<Release> {
    val dates =
        runCatching {
            assets.open(RELEASE_DATES).bufferedReader().use { parseReleaseDates(it.readText()) }
        }.getOrDefault(emptyMap())
    return assets
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
            val name = code.toString().toList().joinToString(".")
            Release(
                versionCode = code,
                versionName = name,
                notes = parseNotes(assets.open("$code.txt").bufferedReader().use { it.readText() }),
                releasedOn = dates[name],
            )
        }
}

fun parseNotes(text: String): List<String> =
    text
        .lines()
        .map { it.trim().replaceFirst(BULLET, "").trim() }
        .filter { it.isNotEmpty() }

fun parseReleaseDates(text: String): Map<String, LocalDate> =
    text
        .lines()
        .mapNotNull { line ->
            val (tag, date) = line.trim().split(' ').takeIf { it.size == 2 } ?: return@mapNotNull null
            val parsed = runCatching { LocalDate.parse(date) }.getOrNull() ?: return@mapNotNull null
            tag.removePrefix("v") to parsed
        }.toMap()

@Composable
fun rememberReleases(): State<List<Release>?> {
    val assets = LocalContext.current.assets
    return produceState<List<Release>?>(null, assets) {
        value = withContext(Dispatchers.IO) { loadReleases(assets) }
    }
}
