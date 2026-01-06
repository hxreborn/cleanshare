package eu.hxreborn.cleanshare

import android.app.ActivityManager
import android.content.IntentFilter
import android.content.pm.ShortcutManager
import eu.hxreborn.cleanshare.hook.LowRamHooker
import eu.hxreborn.cleanshare.hook.ShareTargetsHooker
import io.github.libxposed.api.XposedInterface
import io.github.libxposed.api.XposedModule
import io.github.libxposed.api.XposedModuleInterface.ModuleLoadedParam
import io.github.libxposed.api.XposedModuleInterface.PackageLoadedParam

internal lateinit var module: CleanShareModule

class CleanShareModule(
    base: XposedInterface,
    param: ModuleLoadedParam,
) : XposedModule(base, param) {
    init {
        module = this
        log("v${BuildConfig.VERSION_NAME} loaded")
    }

    override fun onPackageLoaded(param: PackageLoadedParam) {
        if (!param.isFirstPackage) return

        when (param.packageName) {
            INTENT_RESOLVER_PKG -> hookIntentResolver()
            AIAI_PKG -> hookAiAi()
        }
    }

    private fun hookIntentResolver() {
        // Spoof low-RAM so IntentResolver skips creating ShortcutLoader
        runCatching {
            ActivityManager::class.java.getDeclaredMethod("isLowRamDeviceStatic").apply {
                isAccessible = true
                hook(this, LowRamHooker::class.java)
                log("Hooked ActivityManager.isLowRamDeviceStatic")
            }
        }.onFailure {
            log("Failed to hook ActivityManager.isLowRamDeviceStatic", it)
        }
    }

    private fun hookAiAi() {
        // Prevent backend profiling
        runCatching {
            ShortcutManager::class.java.getDeclaredMethod("getShareTargets", IntentFilter::class.java).apply {
                isAccessible = true
                hook(this, ShareTargetsHooker::class.java)
                log("Hooked ShortcutManager.getShareTargets")
            }
        }.onFailure {
            log("Failed to hook ShortcutManager.getShareTargets", it)
        }
    }

    companion object {
        private const val INTENT_RESOLVER_PKG = "com.android.intentresolver"
        private const val AIAI_PKG = "com.google.android.as"
    }
}
