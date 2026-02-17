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
    fun getBoolean(pref: BoolPref): Boolean = localPrefs.getBoolean(pref.key, pref.default)

    fun setBoolean(
        pref: BoolPref,
        value: Boolean,
    ) {
        localPrefs.edit { putBoolean(pref.key, value) }
        remotePrefsProvider()?.edit { putBoolean(pref.key, value) }
    }

    fun observeBoolean(pref: BoolPref): Flow<Boolean> =
        callbackFlow {
            val listener =
                SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
                    if (key == pref.key) {
                        trySend(getBoolean(pref))
                    }
                }
            trySend(getBoolean(pref))
            localPrefs.registerOnSharedPreferenceChangeListener(listener)
            awaitClose { localPrefs.unregisterOnSharedPreferenceChangeListener(listener) }
        }
}
