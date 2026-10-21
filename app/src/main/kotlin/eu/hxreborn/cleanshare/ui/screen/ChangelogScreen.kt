@file:Suppress("ktlint:standard:function-naming")

package eu.hxreborn.cleanshare.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.RemoveCircleOutline
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.Update
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumFlexibleTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import eu.hxreborn.cleanshare.BuildConfig
import eu.hxreborn.cleanshare.R
import eu.hxreborn.cleanshare.ui.component.BackButton
import eu.hxreborn.cleanshare.ui.component.ScreenGutter
import eu.hxreborn.cleanshare.ui.util.Note
import eu.hxreborn.cleanshare.ui.util.NoteKind
import eu.hxreborn.cleanshare.ui.util.Release
import eu.hxreborn.cleanshare.ui.util.centeredContentWidth
import eu.hxreborn.cleanshare.ui.util.drawVerticalScrollbar
import eu.hxreborn.cleanshare.ui.util.rememberReleases
import kotlinx.coroutines.launch

private val BadgeRadius = 8.dp
private val BadgeHeight = 22.dp
private val BadgeIconSize = 14.dp
private val NoteMinHeight = 40.dp

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ChangelogScreen(onBack: () -> Unit) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val releases by rememberReleases()
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val canScrollUp by remember { derivedStateOf { listState.canScrollBackward } }
    val canScrollDown by remember { derivedStateOf { listState.canScrollForward } }
    val navBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        contentWindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal),
        topBar = {
            MediumFlexibleTopAppBar(
                title = { Text(stringResource(R.string.changelog)) },
                navigationIcon = { BackButton(onBack) },
                scrollBehavior = scrollBehavior,
            )
        },
    ) { innerPadding ->
        val list = releases
        Box(Modifier.fillMaxSize()) {
            if (list == null) {
                LoadingIndicator(Modifier.align(Alignment.Center))
                return@Box
            }
            LazyColumn(
                state = listState,
                modifier = Modifier.centeredContentWidth().drawVerticalScrollbar(listState),
                contentPadding =
                    PaddingValues(
                        start = ScreenGutter,
                        end = ScreenGutter,
                        top = innerPadding.calculateTopPadding() + 8.dp,
                        bottom = navBottom + 88.dp,
                    ),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(list, key = { it.versionCode }) { release -> ReleaseCard(release) }
            }
            Column(
                modifier =
                    Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = ScreenGutter, bottom = navBottom + 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                JumpButton(canScrollUp, Icons.Rounded.KeyboardArrowUp, stringResource(R.string.changelog_jump_top)) {
                    scope.launch { listState.animateScrollToItem(0) }
                }
                JumpButton(canScrollDown, Icons.Rounded.KeyboardArrowDown, stringResource(R.string.changelog_jump_bottom)) {
                    scope.launch { listState.animateScrollToItem(list.lastIndex) }
                }
            }
        }
    }
}

@Composable
private fun JumpButton(
    visible: Boolean,
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    AnimatedVisibility(visible = visible, enter = fadeIn() + scaleIn(), exit = fadeOut() + scaleOut()) {
        SmallFloatingActionButton(
            onClick = onClick,
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        ) {
            Icon(icon, contentDescription = label)
        }
    }
}

@Composable
private fun ReleaseCard(release: Release) {
    Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surfaceContainer, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "v${release.versionName}",
                    style = MaterialTheme.typography.titleMedium.copy(fontFamily = FontFamily.Monospace),
                    modifier = Modifier.weight(1f),
                )
                if (release.versionCode == BuildConfig.VERSION_CODE) {
                    Surface(shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.secondaryContainer) {
                        Text(
                            stringResource(R.string.changelog_installed),
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        )
                    }
                }
            }
            ReleaseNotes(release.notes)
        }
    }
}

@Composable
fun ReleaseNotes(notes: List<Note>) {
    Column {
        notes.sortedBy { it.kind.ordinal }.forEach { NoteRow(it) }
    }
}

@Composable
private fun NoteRow(note: Note) {
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = NoteMinHeight)
            .padding(horizontal = 4.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        KindBadge(note.kind)
        Text(note.text, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun KindBadge(kind: NoteKind) {
    val scheme = MaterialTheme.colorScheme
    val (label, icon, colors) =
        when (kind) {
            NoteKind.NEW -> {
                Triple(R.string.notes_new, Icons.Outlined.AutoAwesome, scheme.primaryContainer to scheme.onPrimaryContainer)
            }

            NoteKind.FIXED -> {
                Triple(R.string.notes_fixed, Icons.Outlined.Build, scheme.tertiaryContainer to scheme.onTertiaryContainer)
            }

            NoteKind.UPDATED -> {
                Triple(R.string.notes_updated, Icons.Outlined.Update, scheme.surfaceContainerHighest to scheme.onSurfaceVariant)
            }

            NoteKind.REMOVED -> {
                Triple(
                    R.string.notes_removed,
                    Icons.Outlined.RemoveCircleOutline,
                    scheme.surfaceContainerHighest to scheme.onSurfaceVariant,
                )
            }

            NoteKind.CHANGED -> {
                Triple(R.string.notes_changed, Icons.Outlined.Tune, scheme.surfaceContainerHighest to scheme.onSurfaceVariant)
            }
        }
    Surface(shape = RoundedCornerShape(BadgeRadius), color = colors.first, contentColor = colors.second) {
        Row(
            Modifier.heightIn(min = BadgeHeight).padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(BadgeIconSize))
            Text(
                stringResource(label),
                style = MaterialTheme.typography.labelSmallEmphasized,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
fun WhatsNewSheet(
    release: Release?,
    onFullChangelog: () -> Unit,
    onClose: () -> Unit,
) {
    Column(
        Modifier.padding(start = 16.dp, end = 16.dp, bottom = 20.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            stringResource(R.string.whats_new_title, BuildConfig.VERSION_NAME),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(top = 4.dp).semantics { heading() },
        )
        if (release == null) {
            Text(
                stringResource(R.string.whats_new_missing),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            ReleaseNotes(release.notes)
        }
        FlowRow(
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TextButton(onClick = onClose, shapes = ButtonDefaults.shapes()) {
                Icon(Icons.Outlined.Close, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
                Spacer(Modifier.width(ButtonDefaults.IconSpacing))
                Text(stringResource(R.string.close), maxLines = 1)
            }
            FilledTonalButton(onClick = onFullChangelog, shapes = ButtonDefaults.shapes()) {
                Icon(Icons.Outlined.History, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
                Spacer(Modifier.width(ButtonDefaults.IconSpacing))
                Text(stringResource(R.string.changelog_see_full), maxLines = 1)
            }
        }
    }
}
