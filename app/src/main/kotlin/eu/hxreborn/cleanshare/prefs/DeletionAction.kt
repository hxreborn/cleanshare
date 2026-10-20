package eu.hxreborn.cleanshare.prefs

import androidx.annotation.StringRes
import eu.hxreborn.cleanshare.R

enum class DeletionAction(
    val key: String,
    @StringRes val labelRes: Int,
    @StringRes val summaryRes: Int,
) {
    DELETE("delete", R.string.deletion_action_delete, R.string.pref_deletion_action_summary_delete),
    TRASH("trash", R.string.deletion_action_trash, R.string.pref_deletion_action_summary_trash),
    ;

    companion object {
        fun fromKey(key: String): DeletionAction = entries.firstOrNull { it.key == key } ?: TRASH
    }
}
