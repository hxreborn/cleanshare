package eu.hxreborn.cleanshare.prefs

import androidx.annotation.StringRes
import eu.hxreborn.cleanshare.R

enum class DeletionMode(
    val key: String,
    @StringRes val labelRes: Int,
    @StringRes val summaryRes: Int,
) {
    ASK_EACH_TIME("ask", R.string.deletion_mode_ask, R.string.pref_deletion_mode_summary_ask),
    ALWAYS("always", R.string.deletion_mode_always, R.string.pref_deletion_mode_summary_always),
    ;

    companion object {
        fun fromKey(key: String): DeletionMode =
            entries.firstOrNull { it.key == key } ?: ASK_EACH_TIME
    }
}
