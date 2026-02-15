package eu.hxreborn.cleanshare.util

internal const val PREFS_NAME = "cleanshare"
internal const val PREF_DELETE_AFTER_SHARE = "delete_after_share"

// Broadcast
internal const val ACTION_DELETE_SCREENSHOT = "eu.hxreborn.cleanshare.DELETE_SCREENSHOT"
internal const val DELETE_DELAY_MS = 10_000L

internal const val CHECKBOX_VIEW_TAG = "cleanshare_checkbox"

internal val SCREENSHOT_NAME_PATTERN = Regex("[Ss]creenshot[_-]\\d{8}[_-]\\d{6}.*")

// A11-12 UI dimensions
// https://cs.android.com/android/platform/superproject/+/android-11.0.0_r1:frameworks/base/core/res/res/layout/chooser_grid.xml;l=46
// https://cs.android.com/android/platform/superproject/+/android-11.0.0_r1:frameworks/base/core/res/res/values/dimens.xml;l=818
internal const val CHECKBOX_TEXT_SIZE_SP = 14f
internal const val CHECKBOX_MARGIN_TOP_DP = 18

// A13+ dimensions
// https://cs.android.com/android/platform/superproject/+/master:packages/modules/IntentResolver/java/res/layout/chooser_headline_row.xml;l=8
// https://cs.android.com/android/platform/superproject/+/master:packages/modules/IntentResolver/java/res/values/dimens.xml;l=55
internal const val CHECKBOX_MARGIN_END_DP = 16
internal const val HEADLINE_ROW_HEIGHT_DP = 24
