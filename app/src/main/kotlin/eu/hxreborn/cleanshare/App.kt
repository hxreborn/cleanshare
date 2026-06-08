package eu.hxreborn.cleanshare

import android.app.Application
import android.content.Context
import android.util.Log
import com.topjohnwu.superuser.Shell
import eu.hxreborn.cleanshare.prefs.PrefsRepository
import eu.hxreborn.cleanshare.util.PREFS_FILE_NAME
import io.github.libxposed.service.XposedService
import io.github.libxposed.service.XposedServiceHelper

class App :
    Application(),
    XposedServiceHelper.OnServiceListener {
    @Volatile
    private var mService: XposedService? = null

    lateinit var prefs: PrefsRepository
        private set

    override fun onCreate() {
        super.onCreate()
        val local = getSharedPreferences(PREFS_FILE_NAME, MODE_PRIVATE)
        prefs =
            PrefsRepository(local) {
                runCatching { mService?.getRemotePreferences(PREFS_FILE_NAME) }.getOrNull()
            }
        XposedServiceHelper.registerListener(this)
    }

    override fun onServiceBind(service: XposedService) {
        Log.i(TAG, "service bound name=${service.frameworkName} v=${service.frameworkVersion}")
        mService = service
        prefs.syncToRemote()
    }

    override fun onServiceDied(service: XposedService) {
        Log.w(TAG, "service died")
        mService = null
    }

    companion object {
        private const val TAG = "CleanShare"

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
