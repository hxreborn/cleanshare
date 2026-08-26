package eu.hxreborn.cleanshare.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eu.hxreborn.cleanshare.prefs.AppFilterMode
import eu.hxreborn.cleanshare.prefs.DeletionAction
import eu.hxreborn.cleanshare.prefs.DeletionMode
import eu.hxreborn.cleanshare.prefs.Prefs
import eu.hxreborn.cleanshare.prefs.PrefsRepository
import eu.hxreborn.cleanshare.ui.state.SettingsUiState
import eu.hxreborn.cleanshare.util.RootUtils
import eu.hxreborn.cleanshare.util.isLauncherIconVisible
import eu.hxreborn.cleanshare.util.setLauncherIconVisible
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val prefs: PrefsRepository,
    private val applicationContext: Context,
) : ViewModel() {
    private val rootAvailable = MutableStateFlow(false)
    private val launcherIconHidden = MutableStateFlow(false)

    init {
        viewModelScope.launch(Dispatchers.IO) {
            rootAvailable.value = RootUtils.isRootAvailable()
            launcherIconHidden.value = !isLauncherIconVisible(applicationContext)
        }
    }

    val uiState: StateFlow<SettingsUiState> =
        combine(prefs.state, rootAvailable, launcherIconHidden) { app, root, iconHidden ->
            SettingsUiState.Ready(
                hideDirectShare = app.hideDirectShare,
                hideQuickShare = app.hideQuickShare,
                isLauncherIconHidden = iconHidden,
                deletionEnabled = app.deletionEnabled,
                deletionMode = app.deletionMode,
                deletionAction = app.deletionAction,
                deletionDelayMs = app.deletionDelayMs,
                showDeletionToast = app.showDeletionToast,
                screenshotPattern = app.screenshotPattern,
                isRootAvailable = root,
                appFilterMode = app.appFilterMode,
                filteredApps = app.filteredApps,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SettingsUiState.Loading,
        )

    fun setHideDirectShare(enabled: Boolean) = prefs.save(Prefs.HIDE_DIRECT_SHARE, enabled)

    fun setHideQuickShare(enabled: Boolean) = prefs.save(Prefs.HIDE_QUICK_SHARE, enabled)

    fun setDeletionEnabled(enabled: Boolean) = prefs.save(Prefs.DELETION_ENABLED, enabled)

    fun setDeletionMode(mode: DeletionMode) = prefs.save(Prefs.DELETION_MODE, mode)

    fun setDeletionAction(action: DeletionAction) = prefs.save(Prefs.DELETION_ACTION, action)

    fun setDeletionDelayMs(delayMs: Int) = prefs.save(Prefs.DELETION_DELAY_MS, delayMs.coerceIn(5_000, 60_000))

    fun setShowDeletionToast(enabled: Boolean) = prefs.save(Prefs.SHOW_DELETION_TOAST, enabled)

    fun setScreenshotPattern(pattern: String) = prefs.save(Prefs.SCREENSHOT_PATTERN, pattern)

    fun setAppFilterMode(mode: AppFilterMode) = prefs.save(Prefs.APP_FILTER_MODE, mode)

    fun setFilteredApps(packages: Set<String>) = prefs.save(Prefs.FILTERED_APPS, packages)

    fun setLauncherIconHidden(hidden: Boolean) {
        setLauncherIconVisible(applicationContext, !hidden)
        launcherIconHidden.value = hidden
    }
}
