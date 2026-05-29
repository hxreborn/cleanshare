package eu.hxreborn.cleanshare

import android.app.Application
import android.content.Context
import com.topjohnwu.superuser.Shell
import eu.hxreborn.cleanshare.prefs.PrefsRepository
import eu.hxreborn.cleanshare.util.PREFS_FILE_NAME
import io.github.libxposed.service.XposedService
import io.github.libxposed.service.XposedServiceHelper

class App :
    Application(),
    XposedServiceHelper.OnServiceListener {
    @Volatile
    private var boundService: XposedService? = null

    lateinit var prefs: PrefsRepository
        private set

    override fun onCreate() {
        super.onCreate()
        val localPrefs = getSharedPreferences(PREFS_FILE_NAME, MODE_PRIVATE)
        prefs =
            PrefsRepository(localPrefs) {
                runCatching { boundService?.getRemotePreferences(PREFS_FILE_NAME) }.getOrNull()
            }
        XposedServiceHelper.registerListener(this)
    }

    override fun onServiceBind(service: XposedService) {
        boundService = service
        prefs.syncToRemote()
    }

    override fun onServiceDied(service: XposedService) {
        boundService = null
    }

    companion object {
        init {
            Shell.enableVerboseLogging = BuildConfig.DEBUG
            Shell.setDefaultBuilder(
                Shell.Builder
                    .create()
                    .setTimeout(10),
            )
        }

        fun from(context: Context): App = context.applicationContext as App
    }
}
