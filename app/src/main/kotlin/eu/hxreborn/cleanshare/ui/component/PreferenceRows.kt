@file:Suppress("ktlint:standard:function-naming")

package eu.hxreborn.cleanshare.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import eu.hxreborn.cleanshare.ui.theme.Tokens
import me.zhanghai.compose.preference.LocalPreferenceTheme
import me.zhanghai.compose.preference.Preference
import me.zhanghai.compose.preference.PreferenceTheme
import me.zhanghai.compose.preference.preferenceTheme

@Composable
fun sectionPreferenceTheme(): PreferenceTheme =
    preferenceTheme(
        categoryColor = MaterialTheme.colorScheme.primary,
        categoryTextStyle = MaterialTheme.typography.labelLargeEmphasized,
    )

@Composable
fun PreferenceTitle(resId: Int) {
    Text(
        text = stringResource(resId),
        style = MaterialTheme.typography.bodyLarge,
    )
}

fun Modifier.groupedPreference(
    surface: Color,
    shape: Shape,
): Modifier = padding(horizontal = Tokens.ScreenHorizontalPadding).background(color = surface, shape = shape)

inline fun LazyListScope.switchPreference(
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
        val isEnabled = enabled(value)
        val haptics = LocalHapticFeedback.current
        Preference(
            title = { title(value) },
            modifier =
                modifier.toggleable(value, isEnabled, Role.Switch) {
                    haptics.performHapticFeedback(if (it) HapticFeedbackType.ToggleOn else HapticFeedbackType.ToggleOff)
                    onValueChange(it)
                },
            enabled = isEnabled,
            icon = icon?.let { { it(value) } },
            summary = summary?.let { { it(value) } },
            widgetContainer = {
                val theme = LocalPreferenceTheme.current
                Switch(
                    checked = value,
                    onCheckedChange = null,
                    modifier =
                        Modifier.padding(
                            start = theme.horizontalSpacing,
                            top = theme.padding.calculateTopPadding(),
                            end = theme.padding.calculateEndPadding(LocalLayoutDirection.current),
                            bottom = theme.padding.calculateBottomPadding(),
                        ),
                    enabled = isEnabled,
                    thumbContent = {
                        Icon(
                            imageVector = if (value) Icons.Rounded.Check else Icons.Rounded.Close,
                            contentDescription = null,
                            modifier = Modifier.size(SwitchDefaults.IconSize),
                        )
                    },
                )
            },
        )
    }
}
