package eu.hxreborn.cleanshare.prefs

import android.content.SharedPreferences

sealed class PrefSpec<T : Any>(
    val key: String,
    val default: T,
) {
    abstract fun read(prefs: SharedPreferences): T

    abstract fun write(
        editor: SharedPreferences.Editor,
        value: T,
    )

    fun copyTo(
        from: SharedPreferences,
        to: SharedPreferences.Editor,
    ) = write(to, read(from))
}

class BoolPref(
    key: String,
    default: Boolean,
) : PrefSpec<Boolean>(key, default) {
    override fun read(prefs: SharedPreferences): Boolean = prefs.getBoolean(key, default)

    override fun write(
        editor: SharedPreferences.Editor,
        value: Boolean,
    ) {
        editor.putBoolean(key, value)
    }
}

class IntPref(
    key: String,
    default: Int,
) : PrefSpec<Int>(key, default) {
    override fun read(prefs: SharedPreferences): Int = prefs.getInt(key, default)

    override fun write(
        editor: SharedPreferences.Editor,
        value: Int,
    ) {
        editor.putInt(key, value)
    }
}

class StringPref(
    key: String,
    default: String,
) : PrefSpec<String>(key, default) {
    override fun read(prefs: SharedPreferences): String = prefs.getString(key, default) ?: default

    override fun write(
        editor: SharedPreferences.Editor,
        value: String,
    ) {
        editor.putString(key, value)
    }
}

class StringSetPref(
    key: String,
    default: Set<String>,
) : PrefSpec<Set<String>>(key, default) {
    override fun read(prefs: SharedPreferences): Set<String> =
        prefs.getStringSet(key, default) ?: default

    // Kotlin collection classes are absent in the framework process that unparcels this
    override fun write(
        editor: SharedPreferences.Editor,
        value: Set<String>,
    ) {
        editor.putStringSet(key, HashSet(value))
    }
}

class EnumPref<E : Enum<E>>(
    key: String,
    default: E,
    val fromKey: (String) -> E,
    val toKey: (E) -> String,
) : PrefSpec<E>(key, default) {
    override fun read(prefs: SharedPreferences): E {
        val keyValue = prefs.getString(key, toKey(default)) ?: toKey(default)
        return fromKey(keyValue)
    }

    override fun write(
        editor: SharedPreferences.Editor,
        value: E,
    ) {
        editor.putString(key, toKey(value))
    }
}
