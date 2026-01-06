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

private const val INTENT_RESOLVER_PKG = "com.android.intentresolver"
private const val AIAI_PKG = "com.google.android.as"

class CleanShareModule(
    base: XposedInterface,
    param: ModuleLoadedParam,
) : XposedModule(base, param) {
    init {
        log("CleanShare v${BuildConfig.VERSION_NAME} loaded")
    }

    override fun onPackageLoaded(param: PackageLoadedParam) {
        if (!param.isFirstPackage) return

        when (param.packageName) {
            INTENT_RESOLVER_PKG -> hookLowRam()
            AIAI_PKG -> hookShareTargets()
        }
    }

    // Spoof low-RAM so IntentResolver skips creating ShortcutLoader
    private fun hookLowRam() {
        runCatching {
            val method = ActivityManager::class.java.getDeclaredMethod("isLowRamDeviceStatic")
            method.isAccessible = true
            hook(method, LowRamHooker::class.java)
            log("Hooked ${method.declaringClass.simpleName}.${method.name}")
        }.onFailure { log("LowRam hook failed", it) }
    }

    // Block AiAi shortcut queries to prevent share target profiling
    private fun hookShareTargets() {
        runCatching {
            val method = ShortcutManager::class.java
                .getDeclaredMethod("getShareTargets", IntentFilter::class.java)
            method.isAccessible = true
            hook(method, ShareTargetsHooker::class.java)
            log("Hooked ${method.declaringClass.simpleName}.${method.name}")
        }.onFailure { log("ShareTargets hook failed", it) }
    }
}
