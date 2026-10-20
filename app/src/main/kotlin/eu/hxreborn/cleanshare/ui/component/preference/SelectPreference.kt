@file:Suppress("ktlint:standard:function-naming")

package eu.hxreborn.cleanshare.ui.component.preference

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.nonInteractiveScrollbar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import eu.hxreborn.cleanshare.ui.theme.Tokens

@Composable
fun <T> SelectPreference(
    value: T,
    onClick: () -> Unit,
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    icon: @Composable (() -> Unit)? = null,
    summary: @Composable (() -> Unit)? = null,
    valueToText: (T) -> String = { it.toString() },
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(Tokens.PreferencePadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        icon?.let { PreferenceIcon(it) }
        Column(modifier = Modifier.weight(1f)) {
            CompositionLocalProvider(
                LocalContentColor provides MaterialTheme.colorScheme.onSurface,
            ) {
                ProvideTextStyle(MaterialTheme.typography.bodyLarge) {
                    title()
                }
            }
            summary?.let {
                CompositionLocalProvider(
                    LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant,
                ) {
                    ProvideTextStyle(MaterialTheme.typography.bodyMedium) {
                        it()
                    }
                }
            }
        }
        CompositionLocalProvider(
            LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant,
        ) {
            ProvideTextStyle(MaterialTheme.typography.bodyMedium) {
                Text(valueToText(value), modifier = Modifier.padding(start = Tokens.PreferenceHorizontalSpacing))
            }
        }
    }
}

@Composable
fun <T> SelectPreferenceDialog(
    title: @Composable () -> Unit,
    value: T,
    values: List<T>,
    valueToText: (T) -> String,
    onValueChange: (T) -> Unit,
    onDismiss: () -> Unit,
) {
    val scrollState = rememberScrollState()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = title,
        text = {
            Column(
                modifier =
                    Modifier
                        .selectableGroup()
                        .verticalScrollbar(scrollState)
                        .fadingEdges(scrollState)
                        .verticalScroll(scrollState),
            ) {
                values.forEach { option ->
                    val selected = option == value
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .clip(SelectedRowShape)
                                .then(
                                    if (selected) {
                                        Modifier.background(MaterialTheme.colorScheme.secondaryContainer)
                                    } else {
                                        Modifier
                                    },
                                ).selectable(
                                    selected = selected,
                                    onClick = { onValueChange(option) },
                                    role = Role.RadioButton,
                                ).padding(
                                    horizontal = Tokens.PreferencePadding,
                                    vertical = Tokens.SpacingSm,
                                ),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = selected,
                            onClick = null,
                        )
                        Spacer(
                            modifier = Modifier.padding(start = Tokens.PreferenceHorizontalSpacing),
                        )
                        Text(
                            text = valueToText(option),
                            style = MaterialTheme.typography.bodyLarge,
                            color =
                                if (selected) {
                                    MaterialTheme.colorScheme.onSecondaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                },
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(android.R.string.cancel))
            }
        },
    )
}

private val SelectedRowShape = RoundedCornerShape(Tokens.RowCornerRadius)

private fun Modifier.fadingEdges(
    scrollState: ScrollState,
    fadeLength: Dp = Tokens.SpacingLg,
): Modifier =
    this
        .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
        .drawWithContent {
            drawContent()
            val fadePx = fadeLength.toPx()
            if (scrollState.canScrollBackward) {
                drawRect(
                    brush =
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black),
                            startY = 0f,
                            endY = fadePx,
                        ),
                    blendMode = BlendMode.DstIn,
                )
            }
            if (scrollState.canScrollForward) {
                drawRect(
                    brush =
                        Brush.verticalGradient(
                            colors = listOf(Color.Black, Color.Transparent),
                            startY = size.height - fadePx,
                            endY = size.height,
                        ),
                    blendMode = BlendMode.DstIn,
                )
            }
        }

@Composable
private fun Modifier.verticalScrollbar(state: ScrollableState): Modifier =
    state.scrollIndicatorState?.let { nonInteractiveScrollbar(it, Orientation.Vertical) } ?: this
