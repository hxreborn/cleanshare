@file:Suppress("ktlint:standard:function-naming")

package eu.hxreborn.cleanshare.ui.screen

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.SpeakerNotes
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.FolderDelete
import androidx.compose.material.icons.outlined.NearbyOff
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.PhonelinkErase
import androidx.compose.material.icons.outlined.TextFields
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.PriorityHigh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumFlexibleTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import eu.hxreborn.cleanshare.BuildConfig
import eu.hxreborn.cleanshare.ModuleStatus
import eu.hxreborn.cleanshare.R
import eu.hxreborn.cleanshare.prefs.AppFilterMode
import eu.hxreborn.cleanshare.prefs.DeletionAction
import eu.hxreborn.cleanshare.prefs.DeletionMode
import eu.hxreborn.cleanshare.prefs.ThemeMode
import eu.hxreborn.cleanshare.ui.component.ChoiceButtonGroup
import eu.hxreborn.cleanshare.ui.component.MorphingLogo
import eu.hxreborn.cleanshare.ui.component.PreferenceTitle
import eu.hxreborn.cleanshare.ui.component.SectionCard
import eu.hxreborn.cleanshare.ui.component.groupedPreference
import eu.hxreborn.cleanshare.ui.component.preference.SelectPreference
import eu.hxreborn.cleanshare.ui.component.preference.SelectPreferenceDialog
import eu.hxreborn.cleanshare.ui.component.preference.TogglePreferenceWithIcon
import eu.hxreborn.cleanshare.ui.component.rememberLogoPulse
import eu.hxreborn.cleanshare.ui.component.sectionPreferenceTheme
import eu.hxreborn.cleanshare.ui.component.switchPreference
import eu.hxreborn.cleanshare.ui.state.SettingsUiState
import eu.hxreborn.cleanshare.ui.theme.Tokens
import eu.hxreborn.cleanshare.ui.util.RegexEditDialog
import eu.hxreborn.cleanshare.ui.util.centeredContentWidth
import eu.hxreborn.cleanshare.ui.util.drawVerticalScrollbar
import eu.hxreborn.cleanshare.ui.util.shapeForPosition
import eu.hxreborn.cleanshare.util.DEFAULT_SCREENSHOT_PATTERN
import me.zhanghai.compose.preference.Preference
import me.zhanghai.compose.preference.ProvidePreferenceLocals
import me.zhanghai.compose.preference.preference
import me.zhanghai.compose.preference.preferenceCategory
import kotlin.math.roundToInt

private const val SYSTEM_INTELLIGENCE_PACKAGE = "com.google.android.as"
private const val DELAY_MIN_MS = 5_000f
private const val DELAY_MAX_MS = 60_000f
private const val DELAY_STEPS = 10

class HomeActions(
    val onHideDirectShareChange: (Boolean) -> Unit,
    val onHideQuickShareChange: (Boolean) -> Unit,
    val onDeletionEnabledChange: (Boolean) -> Unit,
    val onDeletionModeChange: (DeletionMode) -> Unit,
    val onDeletionActionChange: (DeletionAction) -> Unit,
    val onDeletionDelayChange: (Int) -> Unit,
    val onShowDeletionToastChange: (Boolean) -> Unit,
    val onScreenshotPatternChange: (String) -> Unit,
    val onAppFilterClick: () -> Unit,
    val onThemeModeChange: (ThemeMode) -> Unit,
    val onDynamicColorChange: (Boolean) -> Unit,
    val onLauncherIconHiddenChange: (Boolean) -> Unit,
    val onAboutClick: () -> Unit,
    val onRequestScope: (List<String>) -> Unit,
    val onResume: () -> Unit,
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HomeScreen(
    state: SettingsUiState,
    moduleStatus: ModuleStatus,
    actions: HomeActions,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val context = LocalContext.current
    val shareSheetPackage = remember(context) { shareSheetPackage(context) }
    val shareSheetLabel = remember(context, shareSheetPackage) { appLabel(context, shareSheetPackage) }
    val onResume by rememberUpdatedState(actions.onResume)
    val haptics = LocalHapticFeedback.current
    var showPatternDialog by rememberSaveable { mutableStateOf(false) }
    var showThemeDialog by rememberSaveable { mutableStateOf(false) }

    LifecycleResumeEffect(Unit) {
        onResume()
        onPauseOrDispose { }
    }

    if (showPatternDialog) {
        RegexEditDialog(
            title = stringResource(R.string.pref_screenshot_pattern_title),
            current = state.screenshotPattern,
            default = DEFAULT_SCREENSHOT_PATTERN,
            onConfirm = { pattern ->
                actions.onScreenshotPatternChange(pattern)
                haptics.performHapticFeedback(HapticFeedbackType.Confirm)
                showPatternDialog = false
            },
            onDismiss = { showPatternDialog = false },
        )
    }

    if (showThemeDialog) {
        val themeLabels = ThemeMode.entries.associateWith { stringResource(it.labelRes) }
        SelectPreferenceDialog(
            title = { Text(stringResource(R.string.pref_theme_title)) },
            value = state.themeMode,
            values = ThemeMode.entries,
            valueToText = { themeLabels.getValue(it) },
            onValueChange = {
                if (it != state.themeMode) {
                    haptics.performHapticFeedback(HapticFeedbackType.SegmentTick)
                    actions.onThemeModeChange(it)
                }
                showThemeDialog = false
            },
            onDismiss = { showThemeDialog = false },
        )
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        contentWindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal),
        topBar = {
            MediumFlexibleTopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                scrollBehavior = scrollBehavior,
            )
        },
    ) { innerPadding ->
        val surface = MaterialTheme.colorScheme.surfaceContainerHigh
        val listState = rememberLazyListState()
        ProvidePreferenceLocals(theme = sectionPreferenceTheme()) {
            LazyColumn(
                state = listState,
                modifier =
                    Modifier
                        .centeredContentWidth()
                        .drawVerticalScrollbar(listState)
                        .padding(horizontal = Tokens.ScreenHorizontalPadding),
                contentPadding =
                    PaddingValues(
                        top = innerPadding.calculateTopPadding(),
                        bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding(),
                    ),
            ) {
                item(key = "status") {
                    StatusCard(
                        status = moduleStatus,
                        shareSheetPackage = shareSheetPackage,
                        shareSheetLabel = shareSheetLabel,
                        onRequestScope = {
                            val wanted = listOfNotNull(shareSheetPackage, SYSTEM_INTELLIGENCE_PACKAGE.takeIf { isInstalled(context, it) })
                            actions.onRequestScope(wanted.filterNot { it in (moduleStatus as? ModuleStatus.Active)?.scope.orEmpty() })
                        },
                        modifier = Modifier.padding(vertical = Tokens.SpacingSm),
                    )
                }

                shareSheetItems(state, actions, surface)
                cleanupItems(state, actions, surface) { showPatternDialog = true }
                appearanceItems(state, actions) { showThemeDialog = true }

                preferenceCategory(
                    key = "category_general",
                    title = { Text(stringResource(R.string.section_general)) },
                )

                switchPreference(
                    modifier = Modifier.groupedPreference(surface, shapeForPosition(1, 0)),
                    key = "hide_launcher_icon",
                    value = state.isLauncherIconHidden,
                    icon = { Icon(Icons.Outlined.PhonelinkErase, contentDescription = null) },
                    title = { PreferenceTitle(R.string.pref_hide_launcher_icon_title) },
                    summary = { Text(stringResource(R.string.pref_hide_launcher_icon_summary)) },
                    onValueChange = actions.onLauncherIconHiddenChange,
                )

                preferenceCategory(
                    key = "category_about",
                    title = { Text(stringResource(R.string.section_about)) },
                )

                preference(
                    modifier = Modifier.groupedPreference(surface, shapeForPosition(1, 0)),
                    key = "about",
                    icon = { Icon(Icons.Rounded.Info, contentDescription = null) },
                    title = { PreferenceTitle(R.string.app_name) },
                    summary = { Text("v${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})") },
                    onClick = actions.onAboutClick,
                )

                item { Spacer(Modifier.height(Tokens.SpacingLg)) }
            }
        }
    }
}

private fun LazyListScope.shareSheetItems(
    state: SettingsUiState,
    actions: HomeActions,
    surface: Color,
) {
    preferenceCategory(
        key = "category_share_sheet",
        title = { Text(stringResource(R.string.section_share_sheet)) },
    )

    val count = 3
    switchPreference(
        modifier = Modifier.groupedPreference(surface, shapeForPosition(count, 0)),
        key = "hide_direct_share",
        value = state.hideDirectShare,
        icon = { Icon(Icons.Outlined.VisibilityOff, contentDescription = null) },
        title = { PreferenceTitle(R.string.pref_hide_direct_share_title) },
        summary = { Text(stringResource(R.string.pref_hide_direct_share_summary)) },
        onValueChange = actions.onHideDirectShareChange,
    )

    item { Spacer(Modifier.height(Tokens.PreferenceItemGap)) }

    switchPreference(
        modifier = Modifier.groupedPreference(surface, shapeForPosition(count, 1)),
        key = "hide_quick_share",
        value = state.hideQuickShare,
        icon = { Icon(Icons.Outlined.NearbyOff, contentDescription = null) },
        title = { PreferenceTitle(R.string.pref_hide_quick_share_title) },
        summary = { Text(stringResource(R.string.pref_hide_quick_share_summary)) },
        onValueChange = actions.onHideQuickShareChange,
    )

    item { Spacer(Modifier.height(Tokens.PreferenceItemGap)) }

    preference(
        modifier = Modifier.groupedPreference(surface, shapeForPosition(count, 2)),
        key = "app_filter",
        icon = { Icon(Icons.Outlined.FilterList, contentDescription = null) },
        title = { PreferenceTitle(R.string.pref_app_filter_title) },
        summary = { Text(appFilterSummary(state.appFilterMode, state.filteredApps.size)) },
        onClick = actions.onAppFilterClick,
    )
}

private fun LazyListScope.cleanupItems(
    state: SettingsUiState,
    actions: HomeActions,
    surface: Color,
    onPatternClick: () -> Unit,
) {
    preferenceCategory(
        key = "category_cleanup",
        title = { Text(stringResource(R.string.section_cleanup)) },
    )

    val rootMissing = state.isRootAvailable == false
    val expanded = state.deletionEnabled && !rootMissing
    val count = if (expanded) 6 else 1

    switchPreference(
        modifier = Modifier.groupedPreference(surface, shapeForPosition(count, 0)),
        key = "deletion_enabled",
        value = state.deletionEnabled,
        enabled = { !rootMissing || it },
        icon = { Icon(Icons.Outlined.DeleteOutline, contentDescription = null) },
        title = { PreferenceTitle(R.string.pref_deletion_enabled_title) },
        summary = {
            Text(stringResource(if (rootMissing) R.string.pref_deletion_requires_root else R.string.pref_deletion_enabled_summary))
        },
        onValueChange = actions.onDeletionEnabledChange,
    )

    if (!expanded) return

    item { Spacer(Modifier.height(Tokens.PreferenceItemGap)) }

    item(key = "deletion_mode", contentType = "ChoicePreference") {
        Column(modifier = Modifier.groupedPreference(surface, shapeForPosition(count, 1))) {
            Preference(
                icon = { Icon(Icons.Outlined.Tune, contentDescription = null) },
                title = { PreferenceTitle(R.string.pref_deletion_mode_title) },
                summary = { Text(stringResource(state.deletionMode.summaryRes)) },
            )
            ChoiceButtonGroup(
                options = DeletionMode.entries,
                selected = state.deletionMode,
                onSelect = actions.onDeletionModeChange,
                label = { stringResource(it.labelRes) },
            )
        }
    }

    item { Spacer(Modifier.height(Tokens.PreferenceItemGap)) }

    item(key = "deletion_action", contentType = "ChoicePreference") {
        Column(modifier = Modifier.groupedPreference(surface, shapeForPosition(count, 2))) {
            Preference(
                icon = { Icon(Icons.Outlined.FolderDelete, contentDescription = null) },
                title = { PreferenceTitle(R.string.pref_deletion_action_title) },
                summary = { Text(stringResource(state.deletionAction.summaryRes)) },
            )
            ChoiceButtonGroup(
                options = listOf(DeletionAction.TRASH, DeletionAction.DELETE),
                selected = state.deletionAction,
                onSelect = actions.onDeletionActionChange,
                label = { stringResource(it.labelRes) },
            )
        }
    }

    item { Spacer(Modifier.height(Tokens.PreferenceItemGap)) }

    item(key = "deletion_delay", contentType = "SliderPreference") {
        DeletionDelayItem(
            delayMs = state.deletionDelayMs,
            onDelayChange = actions.onDeletionDelayChange,
            modifier = Modifier.groupedPreference(surface, shapeForPosition(count, 3)),
        )
    }

    item { Spacer(Modifier.height(Tokens.PreferenceItemGap)) }

    switchPreference(
        modifier = Modifier.groupedPreference(surface, shapeForPosition(count, 4)),
        key = "show_deletion_toast",
        value = state.showDeletionToast,
        icon = { Icon(Icons.AutoMirrored.Outlined.SpeakerNotes, contentDescription = null) },
        title = { PreferenceTitle(R.string.pref_show_toast_title) },
        summary = { Text(stringResource(R.string.pref_show_toast_summary)) },
        onValueChange = actions.onShowDeletionToastChange,
    )

    item { Spacer(Modifier.height(Tokens.PreferenceItemGap)) }

    preference(
        modifier = Modifier.groupedPreference(surface, shapeForPosition(count, 5)),
        key = "screenshot_pattern",
        icon = { Icon(Icons.Outlined.TextFields, contentDescription = null) },
        title = { PreferenceTitle(R.string.pref_screenshot_pattern_title) },
        summary = { Text(stringResource(R.string.pref_screenshot_pattern_summary)) },
        onClick = onPatternClick,
    )
}

private fun LazyListScope.appearanceItems(
    state: SettingsUiState,
    actions: HomeActions,
    onThemeClick: () -> Unit,
) {
    preferenceCategory(
        key = "category_app_theme",
        title = { Text(stringResource(R.string.group_app_theme)) },
    )

    item(key = "app_theme_section") {
        val haptics = LocalHapticFeedback.current
        val themeLabel = stringResource(state.themeMode.labelRes)
        SectionCard(
            items =
                listOfNotNull(
                    {
                        SelectPreference(
                            value = state.themeMode,
                            onClick = onThemeClick,
                            icon = { Icon(Icons.Outlined.DarkMode, contentDescription = null) },
                            title = { Text(stringResource(R.string.pref_theme_title)) },
                            summary = { Text(stringResource(R.string.pref_theme_summary)) },
                            valueToText = { themeLabel },
                        )
                    },
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        {
                            TogglePreferenceWithIcon(
                                value = state.dynamicColor,
                                onValueChange = {
                                    haptics.performHapticFeedback(
                                        if (it) HapticFeedbackType.ToggleOn else HapticFeedbackType.ToggleOff,
                                    )
                                    actions.onDynamicColorChange(it)
                                },
                                icon = { Icon(Icons.Outlined.Palette, contentDescription = null) },
                                title = { Text(stringResource(R.string.pref_dynamic_color_title)) },
                                summary = { Text(stringResource(R.string.pref_dynamic_color_summary)) },
                            )
                        }
                    } else {
                        null
                    },
                ),
        )
    }
}

@Composable
private fun DeletionDelayItem(
    delayMs: Int,
    onDelayChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptics = LocalHapticFeedback.current
    var draft by remember(delayMs) { mutableFloatStateOf(delayMs.toFloat()) }
    Column(modifier = modifier) {
        Preference(
            icon = { Icon(Icons.Outlined.Timer, contentDescription = null) },
            title = { PreferenceTitle(R.string.pref_deletion_delay_title) },
            summary = {
                val seconds = (draft / 1000).roundToInt()
                Text(pluralStringResource(R.plurals.pref_deletion_delay_summary, seconds, seconds))
            },
        )
        Slider(
            value = draft,
            onValueChange = {
                if (it != draft) haptics.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
                draft = it
            },
            onValueChangeFinished = { onDelayChange(draft.roundToInt()) },
            valueRange = DELAY_MIN_MS..DELAY_MAX_MS,
            steps = DELAY_STEPS,
            modifier = Modifier.padding(start = Tokens.SpacingLg, end = Tokens.SpacingLg, bottom = Tokens.SpacingSm),
        )
    }
}

private val BadgeSize = 48.dp
private val BadgeIconSize = 28.dp

private data class StatusContent(
    val title: String,
    val body: String?,
    val healthy: Boolean,
    val checking: Boolean,
    val addScope: Boolean,
    val pending: Boolean = false,
)

@Composable
private fun statusContent(
    status: ModuleStatus,
    shareSheetPackage: String,
    shareSheetLabel: String,
): StatusContent =
    when (status) {
        ModuleStatus.Checking -> {
            StatusContent(stringResource(R.string.status_checking_title), null, healthy = true, checking = true, addScope = false)
        }

        ModuleStatus.Inactive -> {
            StatusContent(
                stringResource(R.string.status_inactive_title),
                stringResource(R.string.status_inactive_body),
                healthy = false,
                checking = false,
                addScope = false,
            )
        }

        is ModuleStatus.Active -> {
            if (shareSheetPackage in status.scope) {
                StatusContent(
                    stringResource(R.string.status_active_title),
                    stringResource(R.string.status_framework, status.frameworkName, status.frameworkVersion, status.apiVersion),
                    healthy = true,
                    checking = false,
                    addScope = false,
                )
            } else {
                StatusContent(
                    stringResource(R.string.status_scope_title),
                    when {
                        status.scopeRequestPending -> stringResource(R.string.status_scope_pending)
                        status.scopeRequestError != null -> stringResource(R.string.status_scope_failed, status.scopeRequestError)
                        else -> stringResource(R.string.status_scope_body, shareSheetLabel)
                    },
                    healthy = false,
                    checking = false,
                    addScope = true,
                    pending = status.scopeRequestPending,
                )
            }
        }
    }

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun StatusCard(
    status: ModuleStatus,
    shareSheetPackage: String,
    shareSheetLabel: String,
    onRequestScope: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val content = statusContent(status, shareSheetPackage, shareSheetLabel)
    val haptics = LocalHapticFeedback.current
    val active = status as? ModuleStatus.Active
    val pending = active?.scopeRequestPending == true
    val wasPending = remember { mutableStateOf(pending) }
    LaunchedEffect(pending) {
        if (wasPending.value && !pending) {
            haptics.performHapticFeedback(
                if (active?.scopeRequestError == null) HapticFeedbackType.Confirm else HapticFeedbackType.Reject,
            )
        }
        wasPending.value = pending
    }

    val colors = MaterialTheme.colorScheme
    val containerColor by animateColorAsState(
        if (content.healthy) colors.surfaceContainerHigh else colors.errorContainer,
        label = "statusContainer",
    )
    Surface(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = Tokens.SectionHorizontalMargin),
        shape = RoundedCornerShape(Tokens.RowCornerRadius),
        color = containerColor,
    ) {
        AnimatedContent(
            targetState = content,
            contentKey = { it.title },
            transitionSpec = { fadeIn() togetherWith fadeOut() using SizeTransform(clip = false) },
            label = "statusContent",
        ) { shown ->
            Column(
                modifier = Modifier.padding(Tokens.PreferencePadding),
                verticalArrangement = Arrangement.spacedBy(Tokens.SpacingLg),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Tokens.PreferenceHorizontalSpacing),
                ) {
                    StatusBadge(checking = shown.checking, healthy = shown.healthy)
                    Column(
                        modifier =
                            Modifier
                                .weight(1f)
                                .semantics(mergeDescendants = true) { liveRegion = LiveRegionMode.Polite },
                    ) {
                        Text(
                            text = shown.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = if (shown.healthy) colors.onSurface else colors.onErrorContainer,
                        )
                        shown.body?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (shown.healthy) colors.onSurfaceVariant else colors.onErrorContainer,
                            )
                        }
                    }
                }
                if (shown.addScope) {
                    Button(
                        onClick = onRequestScope,
                        enabled = !shown.pending,
                        shapes = ButtonDefaults.shapes(),
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = colors.error,
                                contentColor = colors.onError,
                            ),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(stringResource(R.string.status_scope_action))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun StatusBadge(
    checking: Boolean,
    healthy: Boolean,
) {
    val colors = MaterialTheme.colorScheme
    if (checking) {
        LoadingIndicator(modifier = Modifier.size(BadgeSize))
        return
    }
    if (healthy) {
        MorphingLogo(pulse = rememberLogoPulse(), modifier = Modifier.size(BadgeSize)) {
            Box(Modifier.matchParentSize().background(colors.primary))
            Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = null,
                tint = colors.onPrimary,
                modifier = Modifier.size(BadgeIconSize),
            )
        }
        return
    }
    Box(
        modifier =
            Modifier
                .size(BadgeSize)
                .background(color = colors.error, shape = MaterialShapes.Sunny.toShape()),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Rounded.PriorityHigh,
            contentDescription = null,
            tint = colors.onError,
            modifier = Modifier.size(BadgeIconSize),
        )
    }
}

@Composable
private fun appFilterSummary(
    mode: AppFilterMode,
    count: Int,
): String =
    when (count) {
        0 -> stringResource(R.string.pref_app_filter_summary_off)
        else -> pluralStringResource(mode.countSummaryRes, count, count)
    }

private fun shareSheetPackage(context: Context): String =
    context.packageManager
        .resolveActivity(Intent(Intent.ACTION_CHOOSER), PackageManager.MATCH_DEFAULT_ONLY)
        ?.activityInfo
        ?.packageName
        ?: "android"

private fun appLabel(
    context: Context,
    packageName: String,
): String =
    runCatching {
        val pm = context.packageManager
        pm.getApplicationInfo(packageName, 0).loadLabel(pm).toString()
    }.getOrDefault(packageName)

private fun isInstalled(
    context: Context,
    packageName: String,
): Boolean = runCatching { context.packageManager.getApplicationInfo(packageName, 0) }.isSuccess
