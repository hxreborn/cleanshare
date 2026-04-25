package eu.hxreborn.cleanshare.ui

import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import eu.hxreborn.cleanshare.App
import eu.hxreborn.cleanshare.prefs.PrefsRepository
import eu.hxreborn.cleanshare.ui.navigation.Screen
import eu.hxreborn.cleanshare.ui.screen.LicensesScreen
import eu.hxreborn.cleanshare.ui.screen.SettingsScreen
import eu.hxreborn.cleanshare.ui.theme.CleanShareTheme
import eu.hxreborn.cleanshare.ui.viewmodel.SettingsViewModel
import eu.hxreborn.cleanshare.ui.viewmodel.SettingsViewModelFactory
import eu.hxreborn.cleanshare.util.PREFS_FILE_NAME
import io.github.libxposed.service.XposedService
import io.github.libxposed.service.XposedServiceHelper

class MainActivity :
    ComponentActivity(),
    XposedServiceHelper.OnServiceListener {
    private var remotePrefs: SharedPreferences? = null

    private val viewModel: SettingsViewModel by viewModels {
        SettingsViewModelFactory(
            PrefsRepository(
                localPrefs = getSharedPreferences(PREFS_FILE_NAME, MODE_PRIVATE),
                remotePrefsProvider = { remotePrefs },
            ),
            applicationContext,
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        App.addServiceListener(this)

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
                                    onNavigateToLicenses = { backStack.add(Screen.Licenses) },
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

    override fun onServiceBind(service: XposedService) {
        remotePrefs = service.getRemotePreferences(PREFS_FILE_NAME)
        viewModel.syncLocalToRemote()
    }

    override fun onServiceDied(service: XposedService) {
        remotePrefs = null
    }

    override fun onDestroy() {
        super.onDestroy()
        App.removeServiceListener(this)
    }
}
