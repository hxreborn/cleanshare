package eu.hxreborn.cleanshare.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import eu.hxreborn.cleanshare.prefs.DeletionMode
import eu.hxreborn.cleanshare.prefs.Prefs
import eu.hxreborn.cleanshare.prefs.PrefsRepository
import eu.hxreborn.cleanshare.ui.state.SettingsUiState
import eu.hxreborn.cleanshare.util.RootUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

abstract class SettingsViewModel : ViewModel() {
    abstract val uiState: StateFlow<SettingsUiState>

    abstract fun setHideDirectShare(enabled: Boolean)

    abstract fun setHideQuickShare(enabled: Boolean)

    abstract fun setDeletionEnabled(enabled: Boolean)

    abstract fun setDeletionMode(mode: DeletionMode)

    abstract fun setDeletionDelayMs(delayMs: Int)

    abstract fun setShowDeletionToast(enabled: Boolean)

    abstract fun setScreenshotPattern(pattern: String)

    abstract fun syncLocalToRemote()
}

class SettingsViewModelImpl(
    private val prefsRepository: PrefsRepository,
) : SettingsViewModel() {
    private val rootAvailable = MutableStateFlow(false)

    init {
        viewModelScope.launch(Dispatchers.IO) {
            rootAvailable.value = RootUtils.isRootAvailable()
        }
    }

    override val uiState: StateFlow<SettingsUiState> =
        combine(
            prefsRepository.observeBoolean(Prefs.HIDE_DIRECT_SHARE),
            prefsRepository.observeBoolean(Prefs.HIDE_QUICK_SHARE),
            prefsRepository.observeBoolean(Prefs.DELETION_ENABLED),
            prefsRepository.observeEnum(Prefs.DELETION_MODE),
            prefsRepository.observeInt(Prefs.DELETION_DELAY_MS),
            prefsRepository.observeBoolean(Prefs.SHOW_DELETION_TOAST),
            prefsRepository.observeString(Prefs.SCREENSHOT_PATTERN),
            rootAvailable,
        ) { values ->
            SettingsUiState.Ready(
                hideDirectShare = values[0] as Boolean,
                hideQuickShare = values[1] as Boolean,
                deletionEnabled = values[2] as Boolean,
                deletionMode = values[3] as DeletionMode,
                deletionDelayMs = values[4] as Int,
                showDeletionToast = values[5] as Boolean,
                screenshotPattern = values[6] as String,
                isRootAvailable = values[7] as Boolean,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SettingsUiState.Loading,
        )

    override fun setHideDirectShare(enabled: Boolean) {
        prefsRepository.setBoolean(Prefs.HIDE_DIRECT_SHARE, enabled)
    }

    override fun setHideQuickShare(enabled: Boolean) {
        prefsRepository.setBoolean(Prefs.HIDE_QUICK_SHARE, enabled)
    }

    override fun setDeletionEnabled(enabled: Boolean) {
        prefsRepository.setBoolean(Prefs.DELETION_ENABLED, enabled)
    }

    override fun setDeletionMode(mode: DeletionMode) {
        prefsRepository.setEnum(Prefs.DELETION_MODE, mode)
    }

    override fun setDeletionDelayMs(delayMs: Int) {
        prefsRepository.setInt(Prefs.DELETION_DELAY_MS, delayMs.coerceIn(5_000, 60_000))
    }

    override fun setShowDeletionToast(enabled: Boolean) {
        prefsRepository.setBoolean(Prefs.SHOW_DELETION_TOAST, enabled)
    }

    override fun setScreenshotPattern(pattern: String) {
        prefsRepository.setString(Prefs.SCREENSHOT_PATTERN, pattern)
    }

    override fun syncLocalToRemote() {
        prefsRepository.syncLocalToRemote()
    }
}

class SettingsViewModelFactory(
    private val prefsRepository: PrefsRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = SettingsViewModelImpl(prefsRepository) as T
}
