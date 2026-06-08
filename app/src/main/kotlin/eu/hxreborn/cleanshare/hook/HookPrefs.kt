package eu.hxreborn.cleanshare.hook

import android.content.SharedPreferences
import eu.hxreborn.cleanshare.prefs.DeletionMode
import eu.hxreborn.cleanshare.prefs.Prefs
import eu.hxreborn.cleanshare.util.DEFAULT_DELETION_DELAY_MS
import eu.hxreborn.cleanshare.util.DEFAULT_SCREENSHOT_PATTERN

@Volatile internal var hideDirectShare: Boolean = true

@Volatile internal var hideQuickShare: Boolean = false

@Volatile internal var deletionEnabled: Boolean = false

@Volatile internal var deletionMode: DeletionMode = DeletionMode.ASK_EACH_TIME

@Volatile internal var deletionDelayMs: Long = DEFAULT_DELETION_DELAY_MS.toLong()

@Volatile internal var screenshotPattern: Regex = Regex(DEFAULT_SCREENSHOT_PATTERN)

internal fun loadHookPrefs(prefs: SharedPreferences) {
    hideDirectShare = Prefs.HIDE_DIRECT_SHARE.read(prefs)
    hideQuickShare = Prefs.HIDE_QUICK_SHARE.read(prefs)
    deletionEnabled = Prefs.DELETION_ENABLED.read(prefs)
    deletionMode = Prefs.DELETION_MODE.read(prefs)
    deletionDelayMs = Prefs.DELETION_DELAY_MS.read(prefs).toLong()
    screenshotPattern =
        runCatching { Regex(Prefs.SCREENSHOT_PATTERN.read(prefs)) }
            .getOrElse { Regex(DEFAULT_SCREENSHOT_PATTERN) }
}
