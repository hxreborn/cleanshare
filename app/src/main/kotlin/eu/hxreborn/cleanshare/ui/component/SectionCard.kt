@file:Suppress("ktlint:standard:function-naming")

package eu.hxreborn.cleanshare.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import eu.hxreborn.cleanshare.ui.theme.Tokens
import eu.hxreborn.cleanshare.ui.util.shapeForPosition

@Composable
fun SectionCard(
    modifier: Modifier = Modifier,
    items: List<@Composable () -> Unit>,
) {
    Column(
        modifier = modifier.padding(horizontal = Tokens.SectionHorizontalMargin),
        verticalArrangement = Arrangement.spacedBy(Tokens.SectionItemSpacing),
    ) {
        items.forEachIndexed { index, item ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = shapeForPosition(items.size, index),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
            ) {
                item()
            }
        }
    }
}
