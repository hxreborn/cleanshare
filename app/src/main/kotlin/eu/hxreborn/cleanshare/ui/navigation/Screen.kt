package eu.hxreborn.cleanshare.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface Screen : NavKey {
    @Serializable
    data object Settings : Screen

    @Serializable
    data object AppFilter : Screen

    @Serializable
    data object Licenses : Screen
}
