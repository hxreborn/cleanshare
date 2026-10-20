@file:Suppress("ktlint:standard:function-naming")

package eu.hxreborn.cleanshare.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import eu.hxreborn.cleanshare.R
import eu.hxreborn.cleanshare.ui.util.shapeForPosition

private val RowMinHeight = 64.dp
private val RowGap = 2.dp
val ScreenGutter = 16.dp

@Composable
fun SectionHeader(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier =
            modifier
                .padding(start = ScreenGutter + 4.dp, end = ScreenGutter, top = 20.dp, bottom = 8.dp)
                .semantics { heading() },
    )
}

@Composable
fun RowGroup(
    rows: List<@Composable (Shape) -> Unit>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(horizontal = ScreenGutter),
        verticalArrangement = Arrangement.spacedBy(RowGap),
    ) {
        rows.forEachIndexed { index, row -> row(shapeForPosition(rows.size, index)) }
    }
}

@Composable
fun RowSurface(
    shape: Shape,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    val color = MaterialTheme.colorScheme.surfaceContainer
    val column: @Composable () -> Unit = {
        Column(
            modifier =
                Modifier
                    .heightIn(min = RowMinHeight)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.Center,
            content = content,
        )
    }
    if (onClick != null) {
        Surface(onClick = onClick, enabled = enabled, shape = shape, color = color, modifier = modifier.fillMaxWidth()) {
            column()
        }
    } else {
        Surface(shape = shape, color = color, modifier = modifier.fillMaxWidth()) { column() }
    }
}

@Composable
fun RowHeadline(
    icon: ImageVector?,
    title: String,
    summary: String?,
    enabled: Boolean = true,
    trailing: @Composable RowScope.() -> Unit = {},
) {
    val alpha = if (enabled) 1f else 0.38f
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha),
                modifier = Modifier.size(24.dp),
            )
        }
        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha),
            )
            if (summary != null) {
                Text(
                    text = summary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha),
                )
            }
        }
        trailing()
    }
}

@Composable
fun NavigationRow(
    shape: Shape,
    icon: ImageVector,
    title: String,
    summary: String?,
    onClick: () -> Unit,
    external: Boolean = false,
) {
    RowSurface(shape = shape, onClick = onClick) {
        RowHeadline(icon = icon, title = title, summary = summary) {
            Icon(
                imageVector = if (external) Icons.AutoMirrored.Outlined.OpenInNew else Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AdaptiveButtonRow(
    count: Int,
    modifier: Modifier = Modifier,
    button: @Composable (index: Int, stacked: Boolean, modifier: Modifier) -> Unit,
) {
    SubcomposeLayout(modifier.fillMaxWidth()) { constraints ->
        val gap = ButtonGroupDefaults.ConnectedSpaceBetween
        val slot = (constraints.maxWidth - gap.roundToPx() * (count - 1)) / count
        val probes = subcompose("probe") { repeat(count) { button(it, false, Modifier) } }
        val stacked = probes.any { it.maxIntrinsicWidth(constraints.maxHeight) > slot }
        val content =
            subcompose(stacked) {
                if (stacked) {
                    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        repeat(count) { button(it, true, Modifier.fillMaxWidth()) }
                    }
                } else {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(gap)) {
                        repeat(count) { button(it, false, Modifier.weight(1f)) }
                    }
                }
            }.first().measure(constraints)
        layout(content.width, content.height) { content.place(0, 0) }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BackButton(onBack: () -> Unit) {
    IconButton(onClick = onBack, shapes = IconButtonDefaults.shapes()) {
        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = stringResource(R.string.back))
    }
}
