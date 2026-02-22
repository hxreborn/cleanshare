package eu.hxreborn.cleanshare

import android.app.Application
import com.topjohnwu.superuser.Shell
import eu.hxreborn.cleanshare.BuildConfig
import io.github.libxposed.service.XposedService
import io.github.libxposed.service.XposedServiceHelper
import java.util.concurrent.CopyOnWriteArrayList

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        XposedServiceHelper.registerListener(
            object : XposedServiceHelper.OnServiceListener {
                override fun onServiceBind(service: XposedService) {
                    App.service = service
                    listeners.forEach { it.onServiceBind(service) }
                }

                override fun onServiceDied(service: XposedService) {
                    App.service = null
                    listeners.forEach { it.onServiceDied(service) }
                }
            },
        )
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

        var service: XposedService? = null
            private set

        private val listeners = CopyOnWriteArrayList<XposedServiceHelper.OnServiceListener>()

        fun addServiceListener(listener: XposedServiceHelper.OnServiceListener) {
            listeners.add(listener)
            service?.let { listener.onServiceBind(it) }
        }

        fun removeServiceListener(listener: XposedServiceHelper.OnServiceListener) {
            listeners.remove(listener)
        }
    }
}
