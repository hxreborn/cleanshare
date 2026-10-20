@file:Suppress("ktlint:standard:function-naming")

package eu.hxreborn.cleanshare.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import eu.hxreborn.cleanshare.App
import eu.hxreborn.cleanshare.ui.navigation.Screen
import eu.hxreborn.cleanshare.ui.screen.AboutScreen
import eu.hxreborn.cleanshare.ui.screen.AppFilterScreen
import eu.hxreborn.cleanshare.ui.screen.ChangelogScreen
import eu.hxreborn.cleanshare.ui.screen.HomeActions
import eu.hxreborn.cleanshare.ui.screen.HomeScreen
import eu.hxreborn.cleanshare.ui.screen.LicensesScreen
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
        val app = App.from(this)

        setContent {
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            val moduleStatus by app.moduleStatus.collectAsStateWithLifecycle()

            CleanShareTheme {
                val backStack = rememberNavBackStack(Screen.Home)
                val pop: () -> Unit = { backStack.removeLastOrNull() }

                Surface(color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxSize()) {
                    NavDisplay(
                        backStack = backStack,
                        onBack = pop,
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
                                entry<Screen.Home> {
                                    HomeScreen(
                                        state = state,
                                        moduleStatus = moduleStatus,
                                        actions =
                                            HomeActions(
                                                onHideDirectShareChange = viewModel::setHideDirectShare,
                                                onHideQuickShareChange = viewModel::setHideQuickShare,
                                                onDeletionEnabledChange = viewModel::setDeletionEnabled,
                                                onDeletionModeChange = viewModel::setDeletionMode,
                                                onDeletionActionChange = viewModel::setDeletionAction,
                                                onDeletionDelayChange = viewModel::setDeletionDelayMs,
                                                onShowDeletionToastChange = viewModel::setShowDeletionToast,
                                                onScreenshotPatternChange = viewModel::setScreenshotPattern,
                                                onAppFilterClick = { backStack.add(Screen.AppFilter) },
                                                onLauncherIconHiddenChange = viewModel::setLauncherIconHidden,
                                                onAboutClick = { backStack.add(Screen.About) },
                                                onRequestScope = app::requestScope,
                                                onResume = {
                                                    viewModel.refreshRootAccess()
                                                    app.refreshScope()
                                                },
                                            ),
                                    )
                                }
                                entry<Screen.AppFilter> {
                                    AppFilterScreen(viewModel = viewModel, onBack = pop)
                                }
                                entry<Screen.About> {
                                    AboutScreen(
                                        moduleStatus = moduleStatus,
                                        onChangelog = { backStack.add(Screen.Changelog) },
                                        onLicenses = { backStack.add(Screen.Licenses) },
                                        onBack = pop,
                                    )
                                }
                                entry<Screen.Changelog> {
                                    ChangelogScreen(onBack = pop)
                                }
                                entry<Screen.Licenses> {
                                    LicensesScreen(onBack = pop)
                                }
                            },
                    )
                }
            }
        }
    }
}
