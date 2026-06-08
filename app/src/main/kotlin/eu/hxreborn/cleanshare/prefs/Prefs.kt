package eu.hxreborn.cleanshare.prefs

import eu.hxreborn.cleanshare.util.DEFAULT_DELETION_DELAY_MS
import eu.hxreborn.cleanshare.util.DEFAULT_SCREENSHOT_PATTERN
import eu.hxreborn.cleanshare.util.PREF_KEY_DELETION_ACTION
import eu.hxreborn.cleanshare.util.PREF_KEY_DELETION_DELAY_MS
import eu.hxreborn.cleanshare.util.PREF_KEY_DELETION_ENABLED
import eu.hxreborn.cleanshare.util.PREF_KEY_DELETION_MODE
import eu.hxreborn.cleanshare.util.PREF_KEY_HIDE_DIRECT_SHARE
import eu.hxreborn.cleanshare.util.PREF_KEY_HIDE_QUICK_SHARE
import eu.hxreborn.cleanshare.util.PREF_KEY_SCREENSHOT_PATTERN
import eu.hxreborn.cleanshare.util.PREF_KEY_SHOW_DELETION_TOAST

object Prefs {
    val HIDE_DIRECT_SHARE = BoolPref(PREF_KEY_HIDE_DIRECT_SHARE, true)

    val HIDE_QUICK_SHARE = BoolPref(PREF_KEY_HIDE_QUICK_SHARE, false)

    val DELETION_ENABLED = BoolPref(PREF_KEY_DELETION_ENABLED, false)

    val DELETION_MODE =
        EnumPref(
            PREF_KEY_DELETION_MODE,
            DeletionMode.ASK_EACH_TIME,
            DeletionMode::fromKey,
            { it.key },
        )

    val DELETION_ACTION =
        EnumPref(
            PREF_KEY_DELETION_ACTION,
            DeletionAction.TRASH,
            DeletionAction::fromKey,
            { it.key },
        )

    val DELETION_DELAY_MS = IntPref(PREF_KEY_DELETION_DELAY_MS, DEFAULT_DELETION_DELAY_MS)

    val SCREENSHOT_PATTERN = StringPref(PREF_KEY_SCREENSHOT_PATTERN, DEFAULT_SCREENSHOT_PATTERN)

    val SHOW_DELETION_TOAST = BoolPref(PREF_KEY_SHOW_DELETION_TOAST, true)

    val all: List<PrefSpec<*>> =
        listOf(
            HIDE_DIRECT_SHARE,
            HIDE_QUICK_SHARE,
            DELETION_ENABLED,
            DELETION_MODE,
            DELETION_ACTION,
            DELETION_DELAY_MS,
            SCREENSHOT_PATTERN,
            SHOW_DELETION_TOAST,
        )
}
