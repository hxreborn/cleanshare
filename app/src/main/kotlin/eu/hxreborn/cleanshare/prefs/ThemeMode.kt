package eu.hxreborn.cleanshare.prefs

import androidx.annotation.StringRes
import eu.hxreborn.cleanshare.R

enum class ThemeMode(
    val key: String,
    @StringRes val labelRes: Int,
) {
    SYSTEM("system", R.string.theme_system),
    LIGHT("light", R.string.theme_light),
    DARK("dark", R.string.theme_dark),
    ;

    companion object {
        fun fromKey(key: String): ThemeMode = entries.firstOrNull { it.key == key } ?: SYSTEM
    }
}
