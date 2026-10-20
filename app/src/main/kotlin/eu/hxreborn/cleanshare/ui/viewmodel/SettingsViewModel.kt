package eu.hxreborn.cleanshare.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eu.hxreborn.cleanshare.prefs.AppFilterMode
import eu.hxreborn.cleanshare.prefs.AppPrefs
import eu.hxreborn.cleanshare.prefs.DeletionAction
import eu.hxreborn.cleanshare.prefs.DeletionMode
import eu.hxreborn.cleanshare.prefs.Prefs
import eu.hxreborn.cleanshare.prefs.PrefsRepository
import eu.hxreborn.cleanshare.prefs.ThemeMode
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
import java.util.concurrent.atomic.AtomicBoolean

class SettingsViewModel(
    private val prefs: PrefsRepository,
    private val applicationContext: Context,
) : ViewModel() {
    private val rootAvailable = MutableStateFlow<Boolean?>(null)
    private val launcherIconHidden = MutableStateFlow(!isLauncherIconVisible(applicationContext))
    private val rootCheckRunning = AtomicBoolean(false)

    fun refreshRootAccess() {
        if (!rootCheckRunning.compareAndSet(false, true)) return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                rootAvailable.value = RootUtils.isRootAvailable()
            } finally {
                rootCheckRunning.set(false)
            }
        }
    }

    val uiState: StateFlow<SettingsUiState> =
        combine(prefs.state, rootAvailable, launcherIconHidden, ::toUiState)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = toUiState(prefs.readAll(), rootAvailable.value, launcherIconHidden.value),
            )

    private fun toUiState(
        app: AppPrefs,
        root: Boolean?,
        iconHidden: Boolean,
    ) = SettingsUiState(
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
        themeMode = app.themeMode,
        dynamicColor = app.dynamicColor,
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

    fun setThemeMode(mode: ThemeMode) = prefs.save(Prefs.THEME_MODE, mode)

    fun setDynamicColor(enabled: Boolean) = prefs.save(Prefs.DYNAMIC_COLOR, enabled)

    fun setLauncherIconHidden(hidden: Boolean) {
        setLauncherIconVisible(applicationContext, !hidden)
        launcherIconHidden.value = hidden
    }
}
