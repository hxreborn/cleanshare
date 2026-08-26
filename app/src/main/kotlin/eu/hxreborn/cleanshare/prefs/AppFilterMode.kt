package eu.hxreborn.cleanshare.prefs

import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import eu.hxreborn.cleanshare.R

enum class AppFilterMode(
    val key: String,
    @StringRes val labelRes: Int,
    @PluralsRes val countSummaryRes: Int,
    @StringRes val zeroCountRes: Int,
) {
    BLOCK(
        "block",
        R.string.app_filter_mode_block,
        R.plurals.pref_app_filter_summary_block,
        R.string.app_filter_none_block,
    ),
    ALLOW(
        "allow",
        R.string.app_filter_mode_allow,
        R.plurals.pref_app_filter_summary_allow,
        R.string.app_filter_none_allow,
    ),
    ;

    companion object {
        fun fromKey(key: String): AppFilterMode = entries.firstOrNull { it.key == key } ?: BLOCK
    }
}
