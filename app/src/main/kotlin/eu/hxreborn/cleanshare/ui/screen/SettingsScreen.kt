@file:Suppress("ktlint:standard:function-naming")

package eu.hxreborn.cleanshare.ui.screen

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.Gavel
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import eu.hxreborn.cleanshare.BuildConfig
import eu.hxreborn.cleanshare.R
import eu.hxreborn.cleanshare.ui.state.SettingsUiState
import eu.hxreborn.cleanshare.ui.theme.Tokens
import eu.hxreborn.cleanshare.ui.util.shapeForPosition
import eu.hxreborn.cleanshare.ui.viewmodel.SettingsViewModel
import me.zhanghai.compose.preference.ProvidePreferenceLocals
import me.zhanghai.compose.preference.SwitchPreference
import me.zhanghai.compose.preference.preference
import me.zhanghai.compose.preference.preferenceCategory

private const val GITHUB_URL = "https://github.com/hxreborn/cleanshare"
private const val ISSUES_URL = "https://github.com/hxreborn/cleanshare/issues/new/choose"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
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
                    onLicensesClick = viewModel::showLicenses,
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
    onLicensesClick: () -> Unit,
) {
    val context = LocalContext.current
    val surface = MaterialTheme.colorScheme.surfaceVariant

    ProvidePreferenceLocals {
        LazyColumn(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
            contentPadding =
                PaddingValues(
                    top = innerPadding.calculateTopPadding(),
                    bottom = innerPadding.calculateBottomPadding() + 16.dp,
                ),
        ) {
            preferenceCategory(
                key = "features",
                title = { Text(stringResource(R.string.pref_category_features)) },
            )

            val featureItemCount = 1
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

            preferenceCategory(
                key = "about",
                title = { Text(stringResource(R.string.pref_category_about)) },
            )

            val aboutItemCount = 4
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
                        imageVector = Icons.Outlined.Info,
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
            )

            item { Spacer(Modifier.height(2.dp)) }

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

            val issuesShape = shapeForPosition(aboutItemCount, 2)
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

            item { Spacer(Modifier.height(2.dp)) }

            val licensesShape = shapeForPosition(aboutItemCount, 3)
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
        }
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
