package eu.hxreborn.cleanshare.prefs

import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onStart
import android.content.SharedPreferences.OnSharedPreferenceChangeListener as OnChangeListener

data class AppPrefs(
    val hideDirectShare: Boolean,
    val hideQuickShare: Boolean,
    val deletionEnabled: Boolean,
    val deletionMode: DeletionMode,
    val deletionAction: DeletionAction,
    val deletionDelayMs: Int,
    val showDeletionToast: Boolean,
    val screenshotPattern: String,
)

class PrefsRepository(
    private val local: SharedPreferences,
    private val remoteProvider: () -> SharedPreferences?,
) {
    fun <T : Any> read(spec: PrefSpec<T>): T = spec.read(local)

    fun <T : Any> save(
        spec: PrefSpec<T>,
        value: T,
    ) {
        local.edit { spec.write(this, value) }
        remoteProvider()?.edit { spec.write(this, value) }
    }

    val state: Flow<AppPrefs> =
        callbackFlow {
            val listener = OnChangeListener { _, _ -> trySend(readAll()) }
            local.registerOnSharedPreferenceChangeListener(listener)
            awaitClose { local.unregisterOnSharedPreferenceChangeListener(listener) }
        }.onStart { emit(readAll()) }
            .distinctUntilChanged()

    fun syncToRemote() {
        val remote = remoteProvider() ?: return
        remote.edit {
            Prefs.all.forEach { it.copyTo(local, this) }
        }
    }

    private fun readAll() =
        AppPrefs(
            hideDirectShare = read(Prefs.HIDE_DIRECT_SHARE),
            hideQuickShare = read(Prefs.HIDE_QUICK_SHARE),
            deletionEnabled = read(Prefs.DELETION_ENABLED),
            deletionMode = read(Prefs.DELETION_MODE),
            deletionAction = read(Prefs.DELETION_ACTION),
            deletionDelayMs = read(Prefs.DELETION_DELAY_MS),
            showDeletionToast = read(Prefs.SHOW_DELETION_TOAST),
            screenshotPattern = read(Prefs.SCREENSHOT_PATTERN),
        )
}
