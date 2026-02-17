package eu.hxreborn.cleanshare.prefs

sealed interface PrefSpec<T> {
    val key: String
    val default: T
}

data class BoolPref(
    override val key: String,
    override val default: Boolean,
) : PrefSpec<Boolean>
