package eu.hxreborn.cleanshare.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface Screen : NavKey {
    @Serializable
    data object Home : Screen

    @Serializable
    data object AppFilter : Screen

    @Serializable
    data object About : Screen

    @Serializable
    data object Changelog : Screen

    @Serializable
    data object Licenses : Screen
}
