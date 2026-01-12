package eu.hxreborn.cleanshare

import android.app.ActivityManager
import android.os.Build
import android.content.IntentFilter
import android.content.pm.ShortcutManager
import eu.hxreborn.cleanshare.hook.LowRamHooker
import eu.hxreborn.cleanshare.hook.ShareTargetsHooker
import io.github.libxposed.api.XposedInterface
import io.github.libxposed.api.XposedModule
import io.github.libxposed.api.XposedModuleInterface.ModuleLoadedParam
import io.github.libxposed.api.XposedModuleInterface.PackageLoadedParam

private const val ANDROID_FRAMEWORK_PKG = "android"
private const val INTENT_RESOLVER_PKG = "com.android.intentresolver"
private const val AIAI_PKG = "com.google.android.as"

// Share sheet lives in framework on 11–12, IntentResolver from 13+
private val SHARE_SHEET_PKG: String =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        INTENT_RESOLVER_PKG
    } else {
        ANDROID_FRAMEWORK_PKG
    }

class CleanShareModule(
    base: XposedInterface,
    param: ModuleLoadedParam,
) : XposedModule(base, param) {
    init {
        log("CleanShare v${BuildConfig.VERSION_NAME} loaded")
    }

    override fun onPackageLoaded(param: PackageLoadedParam) {
        when (param.packageName) {
            SHARE_SHEET_PKG -> hookLowRam()
            AIAI_PKG -> hookShareTargets()
        }
    }

    // Spoof low-RAM so the share sheet (framework/IntentResolver) skips creating ShortcutLoader
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
