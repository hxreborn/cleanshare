@file:Suppress("ktlint:standard:function-naming")

package eu.hxreborn.cleanshare.ui.screen

import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Android
import androidx.compose.material.icons.outlined.Balance
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.Coffee
import androidx.compose.material.icons.outlined.Extension
import androidx.compose.material.icons.outlined.Gavel
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.NewReleases
import androidx.compose.material.icons.outlined.Sell
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumFlexibleTopAppBar
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import eu.hxreborn.cleanshare.BuildConfig
import eu.hxreborn.cleanshare.ModuleStatus
import eu.hxreborn.cleanshare.R
import eu.hxreborn.cleanshare.ui.component.AdaptiveButtonRow
import eu.hxreborn.cleanshare.ui.component.BackButton
import eu.hxreborn.cleanshare.ui.component.MorphingLogo
import eu.hxreborn.cleanshare.ui.component.NavigationRow
import eu.hxreborn.cleanshare.ui.component.RowGroup
import eu.hxreborn.cleanshare.ui.component.RowHeadline
import eu.hxreborn.cleanshare.ui.component.RowSurface
import eu.hxreborn.cleanshare.ui.component.ScreenGutter
import eu.hxreborn.cleanshare.ui.component.SectionHeader
import eu.hxreborn.cleanshare.ui.component.rememberLogoPulse
import eu.hxreborn.cleanshare.ui.util.Release
import eu.hxreborn.cleanshare.ui.util.centeredContentWidth
import eu.hxreborn.cleanshare.ui.util.drawVerticalScrollbar
import eu.hxreborn.cleanshare.ui.util.rememberReleases
import kotlinx.coroutines.launch

private const val REPO = "hxreborn/cleanshare"
private const val REPO_URL = "https://github.com/$REPO"
private const val ISSUES_URL = "$REPO_URL/issues/new/choose"
private const val RELEASES_URL = "$REPO_URL/releases"
private const val LICENSE_URL = "$REPO_URL/blob/main/LICENSE"
private const val KOFI_URL = "https://ko-fi.com/hxreborn"
private const val LICENSE = "GPL-3.0"
private const val ADAPTIVE_ICON_ZOOM = 1.425f

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AboutScreen(
    moduleStatus: ModuleStatus,
    onChangelog: () -> Unit,
    onLicenses: () -> Unit,
    onBack: () -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val uriHandler = LocalUriHandler.current
    var showWhatsNew by rememberSaveable { mutableStateOf(false) }
    val releases by rememberReleases()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        contentWindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal),
        topBar = {
            MediumFlexibleTopAppBar(
                title = { Text(stringResource(R.string.section_about)) },
                navigationIcon = { BackButton(onBack) },
                scrollBehavior = scrollBehavior,
            )
        },
    ) { innerPadding ->
        val listState = rememberLazyListState()
        LazyColumn(
            state = listState,
            modifier =
                Modifier
                    .centeredContentWidth()
                    .drawVerticalScrollbar(listState),
            contentPadding = PaddingValues(top = innerPadding.calculateTopPadding() + 8.dp, bottom = 16.dp),
        ) {
            item(key = "identity") {
                IdentityCard(onOpenUrl = uriHandler::openUri, modifier = Modifier.padding(horizontal = ScreenGutter))
            }
            item(key = "links") {
                Spacer(Modifier.size(16.dp))
                RowGroup(
                    listOf<@Composable (Shape) -> Unit>(
                        { shape ->
                            NavigationRow(
                                shape,
                                Icons.Outlined.NewReleases,
                                stringResource(R.string.whats_new),
                                stringResource(R.string.whats_new_summary, BuildConfig.VERSION_NAME),
                                { showWhatsNew = true },
                            )
                        },
                        { shape ->
                            NavigationRow(
                                shape,
                                Icons.Outlined.History,
                                stringResource(R.string.changelog),
                                stringResource(R.string.changelog_summary),
                                onChangelog,
                            )
                        },
                        { shape ->
                            NavigationRow(
                                shape,
                                ImageVector.vectorResource(R.drawable.ic_github_24),
                                stringResource(R.string.pref_github_title),
                                REPO,
                                { uriHandler.openUri(REPO_URL) },
                                external = true,
                            )
                        },
                        { shape ->
                            NavigationRow(
                                shape,
                                Icons.Outlined.BugReport,
                                stringResource(R.string.pref_issues_title),
                                stringResource(R.string.pref_issues_summary),
                                { uriHandler.openUri(ISSUES_URL) },
                                external = true,
                            )
                        },
                        { shape ->
                            NavigationRow(
                                shape,
                                Icons.Outlined.Gavel,
                                stringResource(R.string.pref_licenses_title),
                                stringResource(R.string.pref_licenses_summary),
                                onLicenses,
                            )
                        },
                    ),
                )
            }
            item(key = "device") {
                SectionHeader(stringResource(R.string.this_device))
                DeviceGroup(moduleStatus)
            }
            item(key = "nav_inset") {
                Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
            }
        }
    }

    if (showWhatsNew) {
        WhatsNewBottomSheet(
            release = releases?.firstOrNull { it.versionCode == BuildConfig.VERSION_CODE },
            onFullChangelog = {
                showWhatsNew = false
                onChangelog()
            },
            onDismiss = { showWhatsNew = false },
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun IdentityCard(
    onOpenUrl: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val easterEgg =
        stringResource(R.string.pref_version_easter_egg, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE, BuildConfig.BUILD_TYPE)
    val meta = MaterialTheme.typography.labelMedium.copy(fontFamily = FontFamily.Monospace)
    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            val logoPulse = rememberLogoPulse()
            MorphingLogo(
                pulse = logoPulse,
                modifier =
                    Modifier
                        .size(88.dp)
                        .clickable(interactionSource = null, indication = null) {
                            logoPulse.tap()
                            Toast.makeText(context, easterEgg, Toast.LENGTH_SHORT).show()
                        },
            ) {
                Box(Modifier.fillMaxSize().background(colorResource(R.color.ic_launcher_background)))
                Image(
                    painter = painterResource(R.drawable.ic_launcher_foreground),
                    contentDescription = null,
                    modifier =
                        Modifier.fillMaxSize().graphicsLayer {
                            scaleX = ADAPTIVE_ICON_ZOOM
                            scaleY = ADAPTIVE_ICON_ZOOM
                        },
                )
            }
            Text(stringResource(R.string.app_name), style = MaterialTheme.typography.headlineSmall)
            Text(
                stringResource(R.string.app_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                AssistChip(
                    onClick = { onOpenUrl(RELEASES_URL) },
                    label = { Text("v${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})", style = meta) },
                    leadingIcon = { Icon(Icons.Outlined.Sell, null, Modifier.size(AssistChipDefaults.IconSize)) },
                )
                AssistChip(
                    onClick = { onOpenUrl(LICENSE_URL) },
                    label = { Text(LICENSE, style = meta) },
                    leadingIcon = { Icon(Icons.Outlined.Balance, null, Modifier.size(AssistChipDefaults.IconSize)) },
                )
            }
            val labels = listOf(stringResource(R.string.star_on_github), stringResource(R.string.buy_me_a_coffee))
            AdaptiveButtonRow(count = labels.size, modifier = Modifier.padding(top = 4.dp)) { index, stacked, itemModifier ->
                val buttonModifier = itemModifier.heightIn(min = 52.dp)
                if (index == 0) {
                    Button(
                        onClick = { onOpenUrl(REPO_URL) },
                        shapes =
                            if (stacked) {
                                ButtonDefaults.shapes()
                            } else {
                                ButtonDefaults.shapes(
                                    ButtonGroupDefaults.connectedLeadingButtonShape,
                                    ButtonGroupDefaults.connectedLeadingButtonPressShape,
                                )
                            },
                        modifier = buttonModifier,
                    ) {
                        Icon(Icons.Outlined.StarOutline, null, Modifier.size(ButtonDefaults.IconSize))
                        Spacer(Modifier.width(ButtonDefaults.IconSpacing))
                        Text(labels[index], maxLines = 1)
                    }
                } else {
                    FilledTonalButton(
                        onClick = { onOpenUrl(KOFI_URL) },
                        shapes =
                            if (stacked) {
                                ButtonDefaults.shapes()
                            } else {
                                ButtonDefaults.shapes(
                                    ButtonGroupDefaults.connectedTrailingButtonShape,
                                    ButtonGroupDefaults.connectedTrailingButtonPressShape,
                                )
                            },
                        modifier = buttonModifier,
                    ) {
                        Icon(Icons.Outlined.Coffee, null, Modifier.size(ButtonDefaults.IconSize))
                        Spacer(Modifier.width(ButtonDefaults.IconSpacing))
                        Text(labels[index], maxLines = 1)
                    }
                }
            }
        }
    }
}

@Composable
private fun DeviceGroup(moduleStatus: ModuleStatus) {
    val active = moduleStatus as? ModuleStatus.Active
    RowGroup(
        listOf<@Composable (Shape) -> Unit>(
            { shape ->
                RowSurface(shape) {
                    RowHeadline(
                        icon = Icons.Outlined.Android,
                        title = stringResource(R.string.device_android),
                        summary = stringResource(R.string.device_android_value, Build.VERSION.RELEASE, Build.VERSION.SDK_INT),
                    )
                }
            },
            { shape ->
                RowSurface(shape) {
                    RowHeadline(
                        icon = Icons.Outlined.Extension,
                        title = stringResource(R.string.device_framework),
                        summary =
                            if (active != null) {
                                stringResource(R.string.device_framework_value, active.frameworkLabel, active.apiVersion)
                            } else {
                                stringResource(R.string.device_framework_none)
                            },
                    )
                }
            },
        ),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WhatsNewBottomSheet(
    release: Release?,
    onFullChangelog: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        WhatsNewSheet(
            release = release,
            onFullChangelog = onFullChangelog,
            onClose = { scope.launch { sheetState.hide() }.invokeOnCompletion { onDismiss() } },
        )
    }
}
