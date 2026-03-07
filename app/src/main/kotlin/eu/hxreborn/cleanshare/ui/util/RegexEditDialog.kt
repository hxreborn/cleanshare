@file:Suppress("ktlint:standard:function-naming")

package eu.hxreborn.cleanshare.ui.util

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import eu.hxreborn.cleanshare.R

private const val REGEX_HELP_URL = "https://regextutorial.org/"

@Composable
fun RegexEditDialog(
    title: String,
    current: String,
    default: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var pattern by remember { mutableStateOf(current) }
    var isValid by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
        title = { Text(title) },
        text = {
            Column {
                OutlinedTextField(
                    value = pattern,
                    onValueChange = { value ->
                        pattern = value
                        isValid = value.isNotBlank() && runCatching { Regex(value) }.isSuccess
                    },
                    label = { Text(stringResource(R.string.regex_label)) },
                    isError = !isValid,
                    supportingText =
                        if (!isValid) {
                            { Text(stringResource(R.string.regex_dialog_invalid_pattern)) }
                        } else {
                            null
                        },
                    minLines = 1,
                    modifier = Modifier.fillMaxWidth(),
                    textStyle =
                        MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace,
                        ),
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.regex_dialog_example),
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = buildHelpText(),
                    style =
                        MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                )
            }
        },
        confirmButton = {},
        dismissButton = {
            Row(modifier = Modifier.fillMaxWidth()) {
                TextButton(
                    onClick = {
                        pattern = default
                        isValid = true
                    },
                    enabled = pattern != default,
                ) {
                    Text(stringResource(R.string.regex_dialog_default))
                }
                Spacer(Modifier.weight(1f))
                TextButton(onClick = onDismiss) {
                    Text(stringResource(android.R.string.cancel))
                }
                TextButton(onClick = { onConfirm(pattern) }, enabled = isValid) {
                    Text(stringResource(android.R.string.ok))
                }
            }
        },
    )
}

@Composable
private fun buildHelpText(): AnnotatedString =
    buildAnnotatedString {
        append(stringResource(R.string.regex_dialog_hint))
        append(" ")
        val link = LinkAnnotation.Url(REGEX_HELP_URL)
        withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary, textDecoration = TextDecoration.Underline)) {
            withLink(link) {
                append(stringResource(R.string.regex_dialog_learn_more))
            }
        }
    }
