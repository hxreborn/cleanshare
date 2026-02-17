package eu.hxreborn.cleanshare.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import eu.hxreborn.cleanshare.prefs.Prefs
import eu.hxreborn.cleanshare.prefs.PrefsRepository
import eu.hxreborn.cleanshare.ui.state.SettingsUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

abstract class SettingsViewModel : ViewModel() {
    abstract val uiState: StateFlow<SettingsUiState>
    abstract val showLicenses: StateFlow<Boolean>

    abstract fun setHideDirectShare(enabled: Boolean)

    abstract fun setHideQuickShare(enabled: Boolean)

    abstract fun showLicenses()

    abstract fun hideLicenses()
}

class SettingsViewModelImpl(
    private val prefsRepository: PrefsRepository,
) : SettingsViewModel() {
    override val uiState: StateFlow<SettingsUiState> =
        combine(
            prefsRepository.observeBoolean(Prefs.HIDE_DIRECT_SHARE),
            prefsRepository.observeBoolean(Prefs.HIDE_QUICK_SHARE),
        ) { hideDirectShare, hideQuickShare ->
            SettingsUiState.Ready(
                hideDirectShare = hideDirectShare,
                hideQuickShare = hideQuickShare,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SettingsUiState.Loading,
        )

    private val _showLicenses = MutableStateFlow(false)
    override val showLicenses: StateFlow<Boolean> = _showLicenses.asStateFlow()

    override fun setHideDirectShare(enabled: Boolean) {
        prefsRepository.setBoolean(Prefs.HIDE_DIRECT_SHARE, enabled)
    }

    override fun setHideQuickShare(enabled: Boolean) {
        prefsRepository.setBoolean(Prefs.HIDE_QUICK_SHARE, enabled)
    }

    override fun showLicenses() {
        _showLicenses.value = true
    }

    override fun hideLicenses() {
        _showLicenses.value = false
    }
}

class SettingsViewModelFactory(
    private val prefsRepository: PrefsRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = SettingsViewModelImpl(prefsRepository) as T
}
