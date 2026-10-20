package eu.hxreborn.cleanshare

import android.app.Application
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.topjohnwu.superuser.Shell
import eu.hxreborn.cleanshare.prefs.PrefsRepository
import eu.hxreborn.cleanshare.util.PREFS_FILE_NAME
import io.github.libxposed.service.XposedService
import io.github.libxposed.service.XposedServiceHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.concurrent.atomic.AtomicBoolean

sealed interface ModuleStatus {
    data object Checking : ModuleStatus

    data object Inactive : ModuleStatus

    data class Active(
        val frameworkName: String,
        val frameworkVersion: String,
        val apiVersion: Int,
        val scope: Set<String>,
        val scopeRequestPending: Boolean = false,
        val scopeRequestError: String? = null,
    ) : ModuleStatus {
        val frameworkLabel: String get() = "$frameworkName $frameworkVersion"
    }
}

class App :
    Application(),
    XposedServiceHelper.OnServiceListener {
    @Volatile
    private var mService: XposedService? = null

    private val scopeRequestRunning = AtomicBoolean(false)

    lateinit var prefs: PrefsRepository
        private set

    private val _moduleStatus = MutableStateFlow<ModuleStatus>(ModuleStatus.Checking)
    val moduleStatus: StateFlow<ModuleStatus> = _moduleStatus.asStateFlow()

    override fun onCreate() {
        super.onCreate()
        val local = getSharedPreferences(PREFS_FILE_NAME, MODE_PRIVATE)
        prefs =
            PrefsRepository(local) {
                runCatching { mService?.getRemotePreferences(PREFS_FILE_NAME) }.getOrNull()
            }
        XposedServiceHelper.registerListener(this)
        Handler(Looper.getMainLooper()).postDelayed(
            { _moduleStatus.compareAndSet(ModuleStatus.Checking, ModuleStatus.Inactive) },
            SERVICE_BIND_GRACE_MS,
        )
    }

    override fun onServiceBind(service: XposedService) {
        Log.i(
            TAG,
            "service bound name=${service.frameworkName} v=${service.frameworkVersion} api=${service.apiVersion}",
        )
        mService = service
        scopeRequestRunning.set(false)
        prefs.syncToRemote()
        _moduleStatus.value =
            ModuleStatus.Active(
                frameworkName = service.frameworkName,
                frameworkVersion = shortVersion(service.frameworkVersion),
                apiVersion = service.apiVersion,
                scope = readScope(service),
            )
    }

    override fun onServiceDied(service: XposedService) {
        Log.w(TAG, "service died")
        mService = null
        scopeRequestRunning.set(false)
        _moduleStatus.value = ModuleStatus.Inactive
    }

    fun refreshScope() {
        val service = mService ?: return
        val scope = readScope(service)
        _moduleStatus.update { if (it is ModuleStatus.Active) it.copy(scope = scope) else it }
    }

    fun requestScope(packages: List<String>) {
        val service = mService ?: return
        if (!scopeRequestRunning.compareAndSet(false, true)) return
        _moduleStatus.update {
            if (it is ModuleStatus.Active) {
                it.copy(
                    scopeRequestPending = true,
                    scopeRequestError = null,
                )
            } else {
                it
            }
        }
        val listener =
            object : XposedService.OnScopeEventListener {
                override fun onScopeRequestApproved(approved: List<String>) =
                    finishScopeRequest(null)

                override fun onScopeRequestFailed(message: String) = finishScopeRequest(message)
            }
        runCatching { service.requestScope(packages, listener) }
            .onFailure { finishScopeRequest(it.message.orEmpty()) }
    }

    private fun finishScopeRequest(error: String?) {
        Log.i(TAG, "scope request finished error=$error")
        scopeRequestRunning.set(false)
        val scope = mService?.let(::readScope)
        _moduleStatus.update {
            if (it is ModuleStatus.Active) {
                it.copy(
                    scope = scope ?: it.scope,
                    scopeRequestPending = false,
                    scopeRequestError = error,
                )
            } else {
                it
            }
        }
    }

    private fun readScope(service: XposedService): Set<String> =
        runCatching {
            service.scope.toSet()
        }.getOrDefault(emptySet())

    private fun shortVersion(version: String): String =
        Regex("""^\S+""").find(version)?.value ?: version

    companion object {
        private const val TAG = "CleanShare"
        private const val SERVICE_BIND_GRACE_MS = 3_000L

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
