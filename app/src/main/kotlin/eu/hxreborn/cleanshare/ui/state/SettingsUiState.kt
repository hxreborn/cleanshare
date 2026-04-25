package eu.hxreborn.cleanshare.ui.state

import eu.hxreborn.cleanshare.prefs.DeletionAction
import eu.hxreborn.cleanshare.prefs.DeletionMode

sealed interface SettingsUiState {
    data object Loading : SettingsUiState

    data class Ready(
        val hideDirectShare: Boolean,
        val hideQuickShare: Boolean,
        val isLauncherIconHidden: Boolean,
        val deletionEnabled: Boolean,
        val deletionMode: DeletionMode,
        val deletionAction: DeletionAction,
        val deletionDelayMs: Int,
        val showDeletionToast: Boolean,
        val screenshotPattern: String,
        val isRootAvailable: Boolean,
    ) : SettingsUiState
}
