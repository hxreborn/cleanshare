package eu.hxreborn.cleanshare.ui.util

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private val MaxContentWidth = 600.dp

fun Modifier.centeredContentWidth(): Modifier = fillMaxSize().wrapContentWidth().widthIn(max = MaxContentWidth).fillMaxWidth()
