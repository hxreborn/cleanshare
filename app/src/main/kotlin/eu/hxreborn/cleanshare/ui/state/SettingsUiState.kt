package eu.hxreborn.cleanshare.ui.state

import eu.hxreborn.cleanshare.prefs.AppFilterMode
import eu.hxreborn.cleanshare.prefs.DeletionAction
import eu.hxreborn.cleanshare.prefs.DeletionMode

data class SettingsUiState(
    val hideDirectShare: Boolean,
    val hideQuickShare: Boolean,
    val isLauncherIconHidden: Boolean,
    val deletionEnabled: Boolean,
    val deletionMode: DeletionMode,
    val deletionAction: DeletionAction,
    val deletionDelayMs: Int,
    val showDeletionToast: Boolean,
    val screenshotPattern: String,
    val isRootAvailable: Boolean?,
    val appFilterMode: AppFilterMode,
    val filteredApps: Set<String>,
)
