package eu.hxreborn.cleanshare.prefs

import androidx.annotation.StringRes
import eu.hxreborn.cleanshare.R

enum class DeletionAction(
    val key: String,
    val displayName: String,
    @StringRes val summaryRes: Int,
) {
    DELETE("delete", "Permanently delete", R.string.pref_deletion_action_summary_delete),
    TRASH("trash", "Move to trash", R.string.pref_deletion_action_summary_trash),
    ;

    companion object {
        fun fromKey(key: String): DeletionAction = entries.firstOrNull { it.key == key } ?: TRASH
    }
}
