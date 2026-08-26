@file:Suppress("ktlint:standard:function-naming")

package eu.hxreborn.cleanshare.ui.screen

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.SpeakerNotes
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.FolderDelete
import androidx.compose.material.icons.outlined.Gavel
import androidx.compose.material.icons.outlined.NearbyOff
import androidx.compose.material.icons.outlined.PhonelinkErase
import androidx.compose.material.icons.outlined.TextFields
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import eu.hxreborn.cleanshare.BuildConfig
import eu.hxreborn.cleanshare.R
import eu.hxreborn.cleanshare.prefs.AppFilterMode
import eu.hxreborn.cleanshare.prefs.DeletionAction
import eu.hxreborn.cleanshare.prefs.DeletionMode
import eu.hxreborn.cleanshare.ui.state.SettingsUiState
import eu.hxreborn.cleanshare.ui.util.RegexEditDialog
import eu.hxreborn.cleanshare.ui.util.drawVerticalScrollbar
import eu.hxreborn.cleanshare.ui.util.shapeForPosition
import eu.hxreborn.cleanshare.ui.viewmodel.SettingsViewModel
import eu.hxreborn.cleanshare.util.DEFAULT_SCREENSHOT_PATTERN
import me.zhanghai.compose.preference.ProvidePreferenceLocals
import me.zhanghai.compose.preference.SwitchPreference
import me.zhanghai.compose.preference.preference
import me.zhanghai.compose.preference.preferenceCategory

private const val GITHUB_URL = "https://github.com/hxreborn/cleanshare"
private const val ISSUES_URL = "https://github.com/hxreborn/cleanshare/issues/new/choose"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateToAppFilter: () -> Unit,
    onNavigateToLicenses: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        contentWindowInsets = WindowInsets.navigationBars,
        topBar = {
            val isExpanded by remember {
                derivedStateOf { scrollBehavior.state.collapsedFraction < 0.5f }
            }
            LargeTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.app_name),
                        style = if (isExpanded) MaterialTheme.typography.headlineLarge else LocalTextStyle.current,
                        maxLines = if (isExpanded) 2 else 1,
                    )
                },
                scrollBehavior = scrollBehavior,
            )
        },
    ) { padding ->
        when (val state = uiState) {
            is SettingsUiState.Loading -> {
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(padding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            is SettingsUiState.Ready -> {
                SettingsContent(
                    innerPadding = padding,
                    state = state,
                    onHideDirectShareChange = viewModel::setHideDirectShare,
                    onHideQuickShareChange = viewModel::setHideQuickShare,
                    onLauncherIconHiddenChange = viewModel::setLauncherIconHidden,
                    onDeletionEnabledChange = viewModel::setDeletionEnabled,
                    onDeletionModeChange = viewModel::setDeletionMode,
                    onDeletionActionChange = viewModel::setDeletionAction,
                    onDeletionDelayChange = viewModel::setDeletionDelayMs,
                    onShowDeletionToastChange = viewModel::setShowDeletionToast,
                    onScreenshotPatternChange = viewModel::setScreenshotPattern,
                    onAppFilterClick = onNavigateToAppFilter,
                    onLicensesClick = onNavigateToLicenses,
                )
            }
        }
    }
}

@Suppress("LocalContextGetResourceValueCall") // resource read is in an onClick handler, not composition
@Composable
private fun SettingsContent(
    innerPadding: PaddingValues,
    state: SettingsUiState.Ready,
    onHideDirectShareChange: (Boolean) -> Unit,
    onHideQuickShareChange: (Boolean) -> Unit,
    onLauncherIconHiddenChange: (Boolean) -> Unit,
    onDeletionEnabledChange: (Boolean) -> Unit,
    onDeletionModeChange: (DeletionMode) -> Unit,
    onDeletionActionChange: (DeletionAction) -> Unit,
    onDeletionDelayChange: (Int) -> Unit,
    onShowDeletionToastChange: (Boolean) -> Unit,
    onScreenshotPatternChange: (String) -> Unit,
    onAppFilterClick: () -> Unit,
    onLicensesClick: () -> Unit,
) {
    val context = LocalContext.current
    val surface = MaterialTheme.colorScheme.surfaceVariant
    var showPatternDialog by rememberSaveable { mutableStateOf(false) }

    if (showPatternDialog) {
        RegexEditDialog(
            title = stringResource(R.string.pref_screenshot_pattern_title),
            current = state.screenshotPattern,
            default = DEFAULT_SCREENSHOT_PATTERN,
            onConfirm = { pattern ->
                onScreenshotPatternChange(pattern)
                showPatternDialog = false
            },
            onDismiss = { showPatternDialog = false },
        )
    }

    val listState = rememberLazyListState()

    ProvidePreferenceLocals {
        LazyColumn(
            state = listState,
            modifier =
                Modifier
                    .fillMaxSize()
                    .drawVerticalScrollbar(listState)
                    .padding(horizontal = 8.dp),
            contentPadding =
                PaddingValues(
                    top = innerPadding.calculateTopPadding(),
                    bottom = innerPadding.calculateBottomPadding() + 32.dp,
                ),
        ) {
            preferenceCategory(
                key = "features",
                title = { Text(stringResource(R.string.pref_category_features)) },
            )

            val featureItemCount = 4
            val hideDirectShareShape = shapeForPosition(featureItemCount, 0)
            switchPreference(
                modifier = Modifier.preferenceCell(hideDirectShareShape, surface),
                key = "hide_direct_share",
                value = state.hideDirectShare,
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.VisibilityOff,
                        contentDescription = null,
                    )
                },
                title = {
                    Text(
                        text = stringResource(R.string.pref_hide_direct_share_title),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                },
                summary = {
                    Text(text = stringResource(R.string.pref_hide_direct_share_summary))
                },
                onValueChange = onHideDirectShareChange,
            )

            item { Spacer(Modifier.height(2.dp)) }

            val hideQuickShareShape = shapeForPosition(featureItemCount, 1)
            switchPreference(
                modifier = Modifier.preferenceCell(hideQuickShareShape, surface),
                key = "hide_quick_share",
                value = state.hideQuickShare,
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.NearbyOff,
                        contentDescription = null,
                    )
                },
                title = {
                    Text(
                        text = stringResource(R.string.pref_hide_quick_share_title),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                },
                summary = {
                    Text(text = stringResource(R.string.pref_hide_quick_share_summary))
                },
                onValueChange = onHideQuickShareChange,
            )

            item { Spacer(Modifier.height(2.dp)) }

            val appFilterShape = shapeForPosition(featureItemCount, 2)
            preference(
                modifier = Modifier.preferenceCell(appFilterShape, surface),
                key = "app_filter",
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.FilterList,
                        contentDescription = null,
                    )
                },
                title = {
                    Text(
                        text = stringResource(R.string.pref_app_filter_title),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                },
                summary = { Text(text = appFilterSummary(state.appFilterMode, state.filteredApps.size)) },
                onClick = onAppFilterClick,
            )

            item { Spacer(Modifier.height(2.dp)) }

            val hideLauncherIconShape = shapeForPosition(featureItemCount, 3)
            switchPreference(
                modifier = Modifier.preferenceCell(hideLauncherIconShape, surface),
                key = "hide_launcher_icon",
                value = state.isLauncherIconHidden,
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.PhonelinkErase,
                        contentDescription = null,
                    )
                },
                title = {
                    Text(
                        text = stringResource(R.string.pref_hide_launcher_icon_title),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                },
                summary = {
                    Text(text = stringResource(R.string.pref_hide_launcher_icon_summary))
                },
                onValueChange = onLauncherIconHiddenChange,
            )

            preferenceCategory(
                key = "deletion",
                title = { Text(stringResource(R.string.pref_category_deletion)) },
            )

            val deletionItemCount = if (state.deletionEnabled) 6 else 1

            val deletionEnabledShape = shapeForPosition(deletionItemCount, 0)
            switchPreference(
                modifier = Modifier.preferenceCell(deletionEnabledShape, surface),
                key = "deletion_enabled",
                value = state.deletionEnabled,
                enabled = { state.isRootAvailable },
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.DeleteOutline,
                        contentDescription = null,
                    )
                },
                title = {
                    Text(
                        text = stringResource(R.string.pref_deletion_enabled_title),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                },
                summary = {
                    Text(
                        text =
                            if (state.isRootAvailable) {
                                stringResource(R.string.pref_deletion_enabled_summary)
                            } else {
                                stringResource(R.string.pref_deletion_requires_root)
                            },
                    )
                },
                onValueChange = onDeletionEnabledChange,
            )

            if (state.deletionEnabled) {
                item { Spacer(Modifier.height(2.dp)) }

                val modeShape = shapeForPosition(deletionItemCount, 1)
                item(key = "deletion_mode") {
                    SegmentedPreferenceItem(
                        icon = Icons.Outlined.Tune,
                        title = stringResource(R.string.pref_deletion_mode_title),
                        summary = stringResource(state.deletionMode.summaryRes),
                        options = DeletionMode.entries,
                        selected = state.deletionMode,
                        label = { it.displayName },
                        onSelected = onDeletionModeChange,
                        modifier = Modifier.preferenceCell(modeShape, surface),
                    )
                }

                item { Spacer(Modifier.height(2.dp)) }

                val actionShape = shapeForPosition(deletionItemCount, 2)
                item(key = "deletion_action") {
                    SegmentedPreferenceItem(
                        icon = Icons.Outlined.FolderDelete,
                        title = stringResource(R.string.pref_deletion_action_title),
                        summary = stringResource(state.deletionAction.summaryRes),
                        options = DeletionAction.entries,
                        selected = state.deletionAction,
                        label = { it.displayName },
                        onSelected = onDeletionActionChange,
                        modifier = Modifier.preferenceCell(actionShape, surface),
                    )
                }

                item { Spacer(Modifier.height(2.dp)) }

                val delayShape = shapeForPosition(deletionItemCount, 3)
                item(key = "deletion_delay") {
                    DeletionDelayItem(
                        delayMs = state.deletionDelayMs,
                        onDelayChange = onDeletionDelayChange,
                        modifier = Modifier.preferenceCell(delayShape, surface),
                    )
                }

                item { Spacer(Modifier.height(2.dp)) }

                val toastShape = shapeForPosition(deletionItemCount, 4)
                switchPreference(
                    modifier = Modifier.preferenceCell(toastShape, surface),
                    key = "show_deletion_toast",
                    value = state.showDeletionToast,
                    icon = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.SpeakerNotes,
                            contentDescription = null,
                        )
                    },
                    title = {
                        Text(
                            text = stringResource(R.string.pref_show_toast_title),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    },
                    summary = {
                        Text(text = stringResource(R.string.pref_show_toast_summary))
                    },
                    onValueChange = onShowDeletionToastChange,
                )

                item { Spacer(Modifier.height(2.dp)) }

                val patternShape = shapeForPosition(deletionItemCount, 5)
                preference(
                    modifier = Modifier.preferenceCell(patternShape, surface),
                    key = "screenshot_pattern",
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.TextFields,
                            contentDescription = null,
                        )
                    },
                    title = {
                        Text(
                            text = stringResource(R.string.pref_screenshot_pattern_title),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    },
                    summary = { Text(text = stringResource(R.string.pref_screenshot_pattern_summary)) },
                    onClick = { showPatternDialog = true },
                )
            }

            preferenceCategory(
                key = "about",
                title = { Text(stringResource(R.string.pref_category_about)) },
            )

            val aboutItemCount = 4

            val versionShape = shapeForPosition(aboutItemCount, 0)
            preference(
                modifier = Modifier.preferenceCell(versionShape, surface),
                key = "app_version",
                icon = {
                    Icon(
                        imageVector = Icons.Rounded.Info,
                        contentDescription = null,
                    )
                },
                title = {
                    Text(
                        text = stringResource(R.string.pref_version_title),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                },
                summary = {
                    Text(text = "v${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
                },
                onClick = {
                    val msg =
                        context.getString(
                            R.string.pref_version_easter_egg,
                            BuildConfig.VERSION_NAME,
                            BuildConfig.VERSION_CODE,
                            BuildConfig.BUILD_TYPE,
                        )
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                },
            )

            item { Spacer(Modifier.height(2.dp)) }

            val gitRepoShape = shapeForPosition(aboutItemCount, 1)
            preference(
                modifier = Modifier.preferenceCell(gitRepoShape, surface),
                key = "git_repo",
                icon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_github_24),
                        contentDescription = null,
                    )
                },
                title = {
                    Text(
                        text = stringResource(R.string.pref_github_title),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                },
                summary = {
                    Text(text = stringResource(R.string.pref_github_summary))
                },
                onClick = { context.openUrl(GITHUB_URL) },
            )

            item { Spacer(Modifier.height(2.dp)) }

            val licensesShape = shapeForPosition(aboutItemCount, 2)
            preference(
                modifier = Modifier.preferenceCell(licensesShape, surface),
                key = "licenses",
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.Gavel,
                        contentDescription = null,
                    )
                },
                title = {
                    Text(
                        text = stringResource(R.string.pref_licenses_title),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                },
                summary = {
                    Text(text = stringResource(R.string.pref_licenses_summary))
                },
                onClick = onLicensesClick,
            )

            item { Spacer(Modifier.height(2.dp)) }

            val issuesShape = shapeForPosition(aboutItemCount, 3)
            preference(
                modifier = Modifier.preferenceCell(issuesShape, surface),
                key = "report_issue",
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.BugReport,
                        contentDescription = null,
                    )
                },
                title = {
                    Text(
                        text = stringResource(R.string.pref_issues_title),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                },
                summary = {
                    Text(text = stringResource(R.string.pref_issues_summary))
                },
                onClick = { context.openUrl(ISSUES_URL) },
            )
        }
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

@Composable
private fun <T> SegmentedPreferenceItem(
    icon: ImageVector,
    title: String,
    summary: String,
    options: List<T>,
    selected: T,
    label: (T) -> String,
    onSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.padding(end = 16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                )
                Text(
                    text = summary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        SingleChoiceSegmentedButtonRow(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(start = 40.dp, top = 8.dp),
        ) {
            options.forEachIndexed { index, option ->
                SegmentedButton(
                    selected = option == selected,
                    onClick = { onSelected(option) },
                    shape = SegmentedButtonDefaults.itemShape(index, options.size),
                ) {
                    Text(label(option), maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}

@Composable
private fun DeletionDelayItem(
    delayMs: Int,
    onDelayChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Outlined.Timer,
                contentDescription = null,
                modifier = Modifier.padding(end = 16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.pref_deletion_delay_title),
                    style = MaterialTheme.typography.bodyLarge,
                )
                Text(
                    text = stringResource(R.string.pref_deletion_delay_summary, delayMs / 1000),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Slider(
            value = delayMs.toFloat(),
            onValueChange = { onDelayChange(it.toInt()) },
            valueRange = 5_000f..60_000f,
            steps = 10,
            modifier = Modifier.padding(start = 40.dp),
        )
    }
}

private inline fun LazyListScope.switchPreference(
    key: String,
    value: Boolean,
    crossinline title: @Composable (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    crossinline enabled: (Boolean) -> Boolean = { true },
    noinline icon: @Composable ((Boolean) -> Unit)? = null,
    noinline summary: @Composable ((Boolean) -> Unit)? = null,
    noinline onValueChange: (Boolean) -> Unit,
) {
    item(key = key, contentType = "SwitchPreference") {
        SwitchPreference(
            value = value,
            title = { title(value) },
            modifier = modifier,
            enabled = enabled(value),
            icon = icon?.let { { it(value) } },
            summary = summary?.let { { it(value) } },
            onValueChange = onValueChange,
        )
    }
}

private fun Modifier.preferenceCell(
    shape: RoundedCornerShape,
    surface: Color,
): Modifier = padding(horizontal = 8.dp).clip(shape).background(color = surface)

private fun Context.openUrl(url: String) {
    startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
}
