package eu.hxreborn.cleanshare.ui.state

sealed interface SettingsUiState {
    data object Loading : SettingsUiState

    data class Ready(
        val hideDirectShare: Boolean,
        val hideQuickShare: Boolean,
    ) : SettingsUiState
}
