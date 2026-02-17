package eu.hxreborn.cleanshare.ui

import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import eu.hxreborn.cleanshare.App
import eu.hxreborn.cleanshare.prefs.PrefsRepository
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
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        App.addServiceListener(this)

        setContent {
            CleanShareTheme {
                val showLicenses by viewModel.showLicenses.collectAsStateWithLifecycle()

                if (showLicenses) {
                    LicensesScreen(onBack = viewModel::hideLicenses)
                } else {
                    SettingsScreen(viewModel)
                }
            }
        }
    }

    override fun onServiceBind(service: XposedService) {
        remotePrefs = service.getRemotePreferences(PREFS_FILE_NAME)
    }

    override fun onServiceDied(service: XposedService) {
        remotePrefs = null
    }

    override fun onDestroy() {
        super.onDestroy()
        App.removeServiceListener(this)
    }
}
