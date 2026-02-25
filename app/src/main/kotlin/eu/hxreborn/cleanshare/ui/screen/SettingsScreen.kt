@file:Suppress("ktlint:standard:function-naming", "AssignedValueIsNeverRead")

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.SpeakerNotes
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Gavel
import androidx.compose.material.icons.outlined.NearbyOff
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import eu.hxreborn.cleanshare.BuildConfig
import eu.hxreborn.cleanshare.R
import eu.hxreborn.cleanshare.prefs.DeletionMode
import eu.hxreborn.cleanshare.ui.state.SettingsUiState
import eu.hxreborn.cleanshare.ui.theme.Tokens
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
    onNavigateToLicenses: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        contentWindowInsets = WindowInsets.navigationBars,
        topBar = {
            LargeTopAppBar(
                title = {
                    val isExpandedSlot =
                        LocalTextStyle.current.fontSize >= MaterialTheme.typography.headlineMedium.fontSize
                    Text(
                        text = stringResource(R.string.app_name),
                        style =
                            if (isExpandedSlot) {
                                MaterialTheme.typography.headlineLarge.copy(
                                    lineHeight = Tokens.ExpandedTitleLineHeight,
                                )
                            } else {
                                LocalTextStyle.current
                            },
                        maxLines = if (isExpandedSlot) Tokens.ExpandedTitleMaxLines else 1,
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
                    onDeletionEnabledChange = viewModel::setDeletionEnabled,
                    onDeletionModeChange = viewModel::setDeletionMode,
                    onDeletionDelayChange = viewModel::setDeletionDelayMs,
                    onShowDeletionToastChange = viewModel::setShowDeletionToast,
                    onScreenshotPatternChange = viewModel::setScreenshotPattern,
                    onLicensesClick = onNavigateToLicenses,
                )
            }
        }
    }
}

@Composable
private fun SettingsContent(
    innerPadding: PaddingValues,
    state: SettingsUiState.Ready,
    onHideDirectShareChange: (Boolean) -> Unit,
    onHideQuickShareChange: (Boolean) -> Unit,
    onDeletionEnabledChange: (Boolean) -> Unit,
    onDeletionModeChange: (DeletionMode) -> Unit,
    onDeletionDelayChange: (Int) -> Unit,
    onShowDeletionToastChange: (Boolean) -> Unit,
    onScreenshotPatternChange: (String) -> Unit,
    onLicensesClick: () -> Unit,
) {
    val context = LocalContext.current
    val surface = MaterialTheme.colorScheme.surfaceVariant
    var showPatternDialog by remember { mutableStateOf(false) }

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

            val featureItemCount = 2
            val hideDirectShareShape = shapeForPosition(featureItemCount, 0)
            switchPreference(
                modifier =
                    Modifier
                        .padding(horizontal = 8.dp)
                        .background(color = surface, shape = hideDirectShareShape)
                        .clip(hideDirectShareShape),
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
                modifier =
                    Modifier
                        .padding(horizontal = 8.dp)
                        .background(color = surface, shape = hideQuickShareShape)
                        .clip(hideQuickShareShape),
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

            preferenceCategory(
                key = "deletion",
                title = { Text(stringResource(R.string.pref_category_deletion)) },
            )

            val deletionItemCount = if (state.deletionEnabled) 5 else 1

            val deletionEnabledShape = shapeForPosition(deletionItemCount, 0)
            switchPreference(
                modifier =
                    Modifier
                        .padding(horizontal = 8.dp)
                        .background(color = surface, shape = deletionEnabledShape)
                        .clip(deletionEnabledShape),
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
                        modifier =
                            Modifier
                                .padding(horizontal = 8.dp)
                                .background(color = surface, shape = modeShape)
                                .clip(modeShape),
                    )
                }

                item { Spacer(Modifier.height(2.dp)) }

                val delayShape = shapeForPosition(deletionItemCount, 2)
                item(key = "deletion_delay") {
                    DeletionDelayItem(
                        delayMs = state.deletionDelayMs,
                        onDelayChange = onDeletionDelayChange,
                        modifier =
                            Modifier
                                .padding(horizontal = 8.dp)
                                .background(color = surface, shape = delayShape)
                                .clip(delayShape),
                    )
                }

                item { Spacer(Modifier.height(2.dp)) }

                val toastShape = shapeForPosition(deletionItemCount, 3)
                switchPreference(
                    modifier =
                        Modifier
                            .padding(horizontal = 8.dp)
                            .background(color = surface, shape = toastShape)
                            .clip(toastShape),
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

                val patternShape = shapeForPosition(deletionItemCount, 4)
                preference(
                    modifier =
                        Modifier
                            .padding(horizontal = 8.dp)
                            .background(color = surface, shape = patternShape)
                            .clip(patternShape),
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

            // 1. App version
            val versionShape = shapeForPosition(aboutItemCount, 0)
            preference(
                modifier =
                    Modifier
                        .padding(horizontal = 8.dp)
                        .background(color = surface, shape = versionShape)
                        .clip(versionShape),
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

            // 2. Source code
            val gitRepoShape = shapeForPosition(aboutItemCount, 1)
            preference(
                modifier =
                    Modifier
                        .padding(horizontal = 8.dp)
                        .background(color = surface, shape = gitRepoShape)
                        .clip(gitRepoShape),
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

            // 3. Licenses
            val licensesShape = shapeForPosition(aboutItemCount, 2)
            preference(
                modifier =
                    Modifier
                        .padding(horizontal = 8.dp)
                        .background(color = surface, shape = licensesShape)
                        .clip(licensesShape),
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

            // 4. Report issue
            val issuesShape = shapeForPosition(aboutItemCount, 3)
            preference(
                modifier =
                    Modifier
                        .padding(horizontal = 8.dp)
                        .background(color = surface, shape = issuesShape)
                        .clip(issuesShape),
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
            modifier = Modifier.fillMaxWidth().padding(start = 40.dp, top = 8.dp),
        ) {
            options.forEachIndexed { index, option ->
                SegmentedButton(
                    selected = option == selected,
                    onClick = { onSelected(option) },
                    shape = SegmentedButtonDefaults.itemShape(index, options.size),
                ) {
                    Text(label(option))
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
    modifier: Modifier = Modifier.fillMaxWidth(),
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

private fun Context.openUrl(url: String) {
    startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
}
