package eu.hxreborn.cleanshare.util

internal const val PREFS_FILE_NAME = "cleanshare"
internal const val PREF_KEY_HIDE_DIRECT_SHARE = "hide_direct_share"
internal const val PREF_KEY_HIDE_QUICK_SHARE = "hide_quick_share"
internal const val PREF_KEY_DELETION_ENABLED = "deletion_enabled"
internal const val PREF_KEY_DELETION_MODE = "deletion_mode"
internal const val PREF_KEY_DELETION_ACTION = "deletion_action"
internal const val PREF_KEY_DELETION_DELAY_MS = "deletion_delay_ms"
internal const val PREF_KEY_SCREENSHOT_PATTERN = "screenshot_pattern"
internal const val PREF_KEY_SHOW_DELETION_TOAST = "show_deletion_toast"
internal const val PREF_KEY_APP_FILTER_MODE = "app_filter_mode"
internal const val PREF_KEY_FILTERED_APPS = "filtered_apps"

internal const val DEFAULT_DELETION_DELAY_MS = 15_000
internal const val DEFAULT_SCREENSHOT_PATTERN = "[Ss]creenshot[_-]\\d{8}[_-]\\d{6}.*"

internal const val QUICK_SHARE_ACTIVITY = "com.google.android.gms.nearby.sharing.main.MainActivity"

internal const val DELETION_PROVIDER_AUTHORITY = "eu.hxreborn.cleanshare.deletion"
internal const val QUERY_PROVIDER_AUTHORITY = "eu.hxreborn.cleanshare.query"

internal const val CHECKBOX_VIEW_TAG = "cleanshare_checkbox"

internal const val CHECKBOX_TEXT_SIZE_SP = 14f
internal const val CHECKBOX_MARGIN_TOP_DP = 18
internal const val CHECKBOX_INSERT_RETRY_DELAY_MS = 100L
