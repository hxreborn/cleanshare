@file:Suppress("ktlint:standard:function-naming")

package eu.hxreborn.cleanshare.ui.component.preference

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import eu.hxreborn.cleanshare.ui.theme.Tokens

internal val PreferenceIconWidth = Tokens.PreferenceIconContainerMinWidth - Tokens.PreferencePadding

@Composable
internal fun PreferenceIcon(icon: @Composable () -> Unit) {
    Box(
        modifier = Modifier.width(PreferenceIconWidth),
        contentAlignment = Alignment.CenterStart,
    ) {
        CompositionLocalProvider(
            LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant,
            content = icon,
        )
    }
}
