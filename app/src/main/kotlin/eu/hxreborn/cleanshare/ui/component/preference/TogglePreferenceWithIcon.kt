@file:Suppress("ktlint:standard:function-naming")

package eu.hxreborn.cleanshare.ui.component.preference

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import eu.hxreborn.cleanshare.ui.theme.Tokens

@Composable
fun TogglePreferenceWithIcon(
    value: Boolean,
    onValueChange: (Boolean) -> Unit,
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    icon: @Composable (() -> Unit)? = null,
    summary: @Composable (() -> Unit)? = null,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .toggleable(value = value, onValueChange = onValueChange, role = Role.Switch)
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
        Switch(
            checked = value,
            onCheckedChange = null,
            modifier = Modifier.padding(start = Tokens.PreferenceHorizontalSpacing),
            thumbContent = {
                Icon(
                    imageVector = if (value) Icons.Rounded.Check else Icons.Rounded.Close,
                    contentDescription = null,
                    modifier = Modifier.size(SwitchDefaults.IconSize),
                )
            },
        )
    }
}
