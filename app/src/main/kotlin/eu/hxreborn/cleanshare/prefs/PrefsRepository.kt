package eu.hxreborn.cleanshare.prefs

import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class PrefsRepository(
    private val localPrefs: SharedPreferences,
    private val remotePrefsProvider: () -> SharedPreferences?,
) {
    fun getBoolean(pref: BoolPref): Boolean = pref.read(localPrefs)

    fun setBoolean(
        pref: BoolPref,
        value: Boolean,
    ) {
        localPrefs.edit { pref.write(this, value) }
        remotePrefsProvider()?.edit { pref.write(this, value) }
    }

    fun observeBoolean(pref: BoolPref): Flow<Boolean> = observe(pref) { getBoolean(pref) }

    fun getInt(pref: IntPref): Int = pref.read(localPrefs)

    fun setInt(
        pref: IntPref,
        value: Int,
    ) {
        localPrefs.edit { pref.write(this, value) }
        remotePrefsProvider()?.edit { pref.write(this, value) }
    }

    fun observeInt(pref: IntPref): Flow<Int> = observe(pref) { getInt(pref) }

    fun getString(pref: StringPref): String = pref.read(localPrefs)

    fun setString(
        pref: StringPref,
        value: String,
    ) {
        localPrefs.edit { pref.write(this, value) }
        remotePrefsProvider()?.edit { pref.write(this, value) }
    }

    fun observeString(pref: StringPref): Flow<String> = observe(pref) { getString(pref) }

    fun <E : Enum<E>> getEnum(pref: EnumPref<E>): E = pref.read(localPrefs)

    fun <E : Enum<E>> setEnum(
        pref: EnumPref<E>,
        value: E,
    ) {
        localPrefs.edit { pref.write(this, value) }
        remotePrefsProvider()?.edit { pref.write(this, value) }
    }

    fun <E : Enum<E>> observeEnum(pref: EnumPref<E>): Flow<E> = observe(pref) { getEnum(pref) }

    fun syncToRemote() {
        val remote = remotePrefsProvider() ?: return
        remote.edit {
            Prefs.all.forEach { it.copyTo(localPrefs, this) }
        }
    }

    private fun <T> observe(
        pref: PrefSpec<*>,
        getValue: () -> T,
    ): Flow<T> =
        callbackFlow {
            val listener =
                SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
                    if (key == pref.key) trySend(getValue())
                }
            trySend(getValue())
            localPrefs.registerOnSharedPreferenceChangeListener(listener)
            awaitClose { localPrefs.unregisterOnSharedPreferenceChangeListener(listener) }
        }
}
