package eu.hxreborn.cleanshare.ui.util

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

private val CornerLarge = 24.dp
private val CornerSmall = 4.dp

fun shapeForPosition(
    count: Int,
    index: Int,
): RoundedCornerShape =
    when {
        count == 1 -> {
            RoundedCornerShape(CornerLarge)
        }

        index == 0 -> {
            RoundedCornerShape(
                topStart = CornerLarge,
                topEnd = CornerLarge,
                bottomEnd = CornerSmall,
                bottomStart = CornerSmall,
            )
        }

        index == count - 1 -> {
            RoundedCornerShape(
                topStart = CornerSmall,
                topEnd = CornerSmall,
                bottomEnd = CornerLarge,
                bottomStart = CornerLarge,
            )
        }

        else -> {
            RoundedCornerShape(CornerSmall)
        }
    }
