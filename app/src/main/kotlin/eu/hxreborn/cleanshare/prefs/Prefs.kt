package eu.hxreborn.cleanshare.prefs

object Prefs {
    val HIDE_DIRECT_SHARE =
        BoolPref(
            key = "hide_direct_share",
            default = true,
        )

    val HIDE_QUICK_SHARE =
        BoolPref(
            key = "hide_quick_share",
            default = false,
        )
}
