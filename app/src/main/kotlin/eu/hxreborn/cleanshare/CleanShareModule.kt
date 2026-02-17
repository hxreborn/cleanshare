package eu.hxreborn.cleanshare

import android.app.ActivityManager
import android.content.IntentFilter
import android.content.pm.ShortcutManager
import android.os.Build
import android.os.Bundle
import eu.hxreborn.cleanshare.hook.deletion.CheckboxHook
import eu.hxreborn.cleanshare.hook.deletion.DeletionHook
import eu.hxreborn.cleanshare.hook.directshare.LowRamHooker
import eu.hxreborn.cleanshare.hook.directshare.ShareTargetsHooker
import eu.hxreborn.cleanshare.hook.quickshare.QuickShareFilterHooker
import eu.hxreborn.cleanshare.util.findClass
import eu.hxreborn.cleanshare.util.findMethodByName
import io.github.libxposed.api.XposedInterface
import io.github.libxposed.api.XposedModule
import io.github.libxposed.api.XposedModuleInterface.ModuleLoadedParam
import io.github.libxposed.api.XposedModuleInterface.PackageLoadedParam

private const val ANDROID_FRAMEWORK_PKG = "android"
private const val INTENT_RESOLVER_PKG = "com.android.intentresolver"
private const val AIAI_PKG = "com.google.android.as"

// ChooserActivity location differs by API level
private val CHOOSER_CLASS_NAMES =
    listOf(
        "com.android.intentresolver.ChooserActivity", // API 33+
        "com.android.internal.app.ChooserActivity", // API 30-32
    )

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
    companion object {
        var instance: CleanShareModule? = null
            private set
    }

    init {
        instance = this
        log("CleanShare v${BuildConfig.VERSION_NAME} loaded in ${param.processName}")
    }

    override fun onPackageLoaded(param: PackageLoadedParam) {
        when (param.packageName) {
            SHARE_SHEET_PKG -> {
                hookLowRam()
                hookScreenshotDelete(param.classLoader)
                hookQuickShareFilter(param.classLoader)
            }

            AIAI_PKG -> {
                hookShareTargets()
            }
        }
    }

    // Spoof low-RAM so the share sheet skips creating ShortcutLoader
    private fun hookLowRam() {
        runCatching {
            val method = ActivityManager::class.java.getDeclaredMethod("isLowRamDeviceStatic")
            method.isAccessible = true
            hook(method, LowRamHooker::class.java)
            log("Hooked ActivityManager.isLowRamDeviceStatic")
        }.onFailure {
            log("LowRam hook failed: ${it.message}")
        }
    }

    // Insert "Delete after sharing" checkbox for screenshot shares
    private fun hookScreenshotDelete(classLoader: ClassLoader) {
        val chooserClass = findClass(classLoader, CHOOSER_CLASS_NAMES)
        if (chooserClass == null) {
            log("ChooserActivity not found, skipping deletion hooks")
            return
        }

        // Hook onCreate for checkbox insertion
        runCatching {
            val method = chooserClass.getDeclaredMethod("onCreate", Bundle::class.java)
            method.isAccessible = true
            hook(method, CheckboxHook::class.java)
            log("Hooked ${chooserClass.simpleName}.onCreate")
        }.onFailure {
            log("Checkbox hook failed: ${it.message}")
        }

        // Hook startSelected for deletion trigger
        val startSelected = findMethodByName(chooserClass, "startSelected")
        if (startSelected == null) {
            log("startSelected not found on ${chooserClass.name}")
            return
        }

        runCatching {
            startSelected.isAccessible = true
            hook(startSelected, DeletionHook::class.java)
            log("Hooked ${chooserClass.simpleName}.startSelected")
        }.onFailure {
            log("Deletion hook failed: ${it.message}")
        }
    }

    // Block AiAi shortcut queries to prevent share target profiling
    private fun hookShareTargets() {
        runCatching {
            val method =
                ShortcutManager::class.java.getDeclaredMethod(
                    "getShareTargets",
                    IntentFilter::class.java,
                )
            method.isAccessible = true
            hook(method, ShareTargetsHooker::class.java)
            log("Hooked ShortcutManager.getShareTargets")
        }.onFailure {
            log("ShareTargets hook failed: ${it.message}")
        }
    }

    // Filter Quick Share from share targets by hooking queryIntentActivitiesAsUser
    // Works on both framework (A11-12) and IntentResolver (A13+)
    private fun hookQuickShareFilter(classLoader: ClassLoader) {
        val pmClass =
            runCatching {
                classLoader.loadClass("android.app.ApplicationPackageManager")
            }.getOrNull() ?: run {
                log("Quick Share hook: ApplicationPackageManager not found")
                return
            }

        val methods = pmClass.declaredMethods.filter { it.name == "queryIntentActivitiesAsUser" }
        if (methods.isEmpty()) {
            log("Quick Share hook: no queryIntentActivitiesAsUser methods found")
            return
        }

        methods.forEach { method ->
            runCatching {
                method.isAccessible = true
                hook(method, QuickShareFilterHooker::class.java)
            }
        }
        log("Hooked queryIntentActivitiesAsUser (${methods.size} overloads)")
    }
}
