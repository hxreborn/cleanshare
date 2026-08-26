package eu.hxreborn.cleanshare.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import eu.hxreborn.cleanshare.App
import eu.hxreborn.cleanshare.ui.navigation.Screen
import eu.hxreborn.cleanshare.ui.screen.AppFilterScreen
import eu.hxreborn.cleanshare.ui.screen.LicensesScreen
import eu.hxreborn.cleanshare.ui.screen.SettingsScreen
import eu.hxreborn.cleanshare.ui.theme.CleanShareTheme
import eu.hxreborn.cleanshare.ui.viewmodel.SettingsViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: SettingsViewModel by viewModels {
        viewModelFactory {
            initializer { SettingsViewModel(App.from(this@MainActivity).prefs, applicationContext) }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            CleanShareTheme {
                val backStack = rememberNavBackStack(Screen.Settings)

                NavDisplay(
                    backStack = backStack,
                    onBack = { backStack.removeLastOrNull() },
                    transitionSpec = {
                        slideInHorizontally(initialOffsetX = { it }) togetherWith
                            slideOutHorizontally(targetOffsetX = { -it })
                    },
                    popTransitionSpec = {
                        slideInHorizontally(initialOffsetX = { -it }) togetherWith
                            slideOutHorizontally(targetOffsetX = { it })
                    },
                    predictivePopTransitionSpec = {
                        slideInHorizontally(initialOffsetX = { -it }) togetherWith
                            slideOutHorizontally(targetOffsetX = { it })
                    },
                    entryProvider =
                        entryProvider {
                            entry<Screen.Settings> {
                                SettingsScreen(
                                    viewModel = viewModel,
                                    onNavigateToAppFilter = { backStack.add(Screen.AppFilter) },
                                    onNavigateToLicenses = { backStack.add(Screen.Licenses) },
                                )
                            }
                            entry<Screen.AppFilter> {
                                AppFilterScreen(
                                    viewModel = viewModel,
                                    onBack = { backStack.removeLastOrNull() },
                                )
                            }
                            entry<Screen.Licenses> {
                                LicensesScreen(onBack = { backStack.removeLastOrNull() })
                            }
                        },
                )
            }
        }
    }
}
