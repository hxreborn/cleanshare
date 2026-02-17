package eu.hxreborn.cleanshare.prefs

import androidx.annotation.StringRes
import eu.hxreborn.cleanshare.R

enum class DeletionMode(
    val key: String,
    val displayName: String,
    @StringRes val summaryRes: Int,
) {
    ASK_EACH_TIME("ask", "Show Checkbox", R.string.pref_deletion_mode_summary_ask),
    ALWAYS("always", "Always Delete", R.string.pref_deletion_mode_summary_always),
    ;

    companion object {
        fun fromKey(key: String): DeletionMode =
            entries.firstOrNull { it.key == key } ?: ASK_EACH_TIME
    }
}
