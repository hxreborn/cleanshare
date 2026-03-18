package eu.hxreborn.cleanshare

import android.app.Activity
import android.app.ActivityManager
import android.content.IntentFilter
import android.content.pm.ResolveInfo
import android.content.pm.ShortcutManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import eu.hxreborn.cleanshare.hook.deletion.CheckboxHook
import eu.hxreborn.cleanshare.hook.deletion.DeletionHook
import eu.hxreborn.cleanshare.util.PREFS_FILE_NAME
import eu.hxreborn.cleanshare.util.PREF_KEY_HIDE_DIRECT_SHARE
import eu.hxreborn.cleanshare.util.PREF_KEY_HIDE_QUICK_SHARE
import eu.hxreborn.cleanshare.util.QUICK_SHARE_ACTIVITY
import eu.hxreborn.cleanshare.util.debugLog
import eu.hxreborn.cleanshare.util.findClass
import eu.hxreborn.cleanshare.util.findMethodByName
import io.github.libxposed.api.XposedModule
import io.github.libxposed.api.XposedModuleInterface.ModuleLoadedParam
import io.github.libxposed.api.XposedModuleInterface.PackageReadyParam

private const val ANDROID_FRAMEWORK_PKG = "android"
private const val INTENT_RESOLVER_PKG = "com.android.intentresolver"
private const val AIAI_PKG = "com.google.android.as"

private val CHOOSER_CLASS_NAMES =
    listOf(
        "com.android.intentresolver.ChooserActivity",
        "com.android.internal.app.ChooserActivity",
    )

private val SHARE_SHEET_PKG: String =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        INTENT_RESOLVER_PKG
    } else {
        ANDROID_FRAMEWORK_PKG
    }

class CleanShareModule : XposedModule() {
    companion object {
        const val TAG = "CleanShare"
        var instance: CleanShareModule? = null
            private set
    }

    override fun onModuleLoaded(param: ModuleLoadedParam) {
        instance = this
        log(Log.INFO, TAG, "v${BuildConfig.VERSION_NAME} loaded in ${param.processName}")
    }

    override fun onPackageReady(param: PackageReadyParam) {
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

    private fun hookLowRam() {
        runCatching {
            val method = ActivityManager::class.java.getDeclaredMethod("isLowRamDeviceStatic")
            method.isAccessible = true
            hook(method).intercept { chain ->
                val prefs = getRemotePreferences(PREFS_FILE_NAME)
                val enabled = prefs?.getBoolean(PREF_KEY_HIDE_DIRECT_SHARE, true) ?: true
                debugLog { "[DirectShare] isLowRamDeviceStatic called, enabled=$enabled" }
                if (enabled) return@intercept true
                chain.proceed()
            }
            log(Log.INFO, TAG, "Hooked ActivityManager.isLowRamDeviceStatic")
        }.onFailure { log(Log.WARN, TAG, "LowRam hook failed: ${it.message}") }
    }

    private fun hookScreenshotDelete(classLoader: ClassLoader) {
        val chooserClass =
            findClass(classLoader, CHOOSER_CLASS_NAMES) ?: run {
                log(Log.WARN, TAG, "ChooserActivity not found, skipping deletion hooks")
                return
            }

        runCatching {
            val method = chooserClass.getDeclaredMethod("onCreate", Bundle::class.java)
            method.isAccessible = true
            hook(method).intercept { chain ->
                chain.proceed()
                CheckboxHook.handleOnCreate(chain.thisObject as? Activity ?: return@intercept null)
                null
            }
            log(Log.INFO, TAG, "Hooked ${chooserClass.simpleName}.onCreate")
        }.onFailure { log(Log.WARN, TAG, "Checkbox hook failed: ${it.message}") }

        val startSelected =
            findMethodByName(chooserClass, "startSelected") ?: run {
                log(Log.WARN, TAG, "startSelected not found on ${chooserClass.name}")
                return
            }

        runCatching {
            startSelected.isAccessible = true
            hook(startSelected).intercept { chain ->
                chain.proceed()
                DeletionHook.handleStartSelected()
                null
            }
            log(Log.INFO, TAG, "Hooked ${chooserClass.simpleName}.startSelected")
        }.onFailure { log(Log.WARN, TAG, "Deletion hook failed: ${it.message}") }
    }

    private fun hookShareTargets() {
        runCatching {
            val method =
                ShortcutManager::class.java
                    .getDeclaredMethod("getShareTargets", IntentFilter::class.java)
            method.isAccessible = true
            hook(method).intercept { chain ->
                val prefs = getRemotePreferences(PREFS_FILE_NAME)
                val enabled = prefs?.getBoolean(PREF_KEY_HIDE_DIRECT_SHARE, true) ?: true
                debugLog { "[DirectShare] getShareTargets called, enabled=$enabled" }
                if (enabled) return@intercept emptyList<Any>()
                chain.proceed()
            }
            log(Log.INFO, TAG, "Hooked ShortcutManager.getShareTargets")
        }.onFailure { log(Log.WARN, TAG, "ShareTargets hook failed: ${it.message}") }
    }

    @Suppress("UNCHECKED_CAST")
    private fun hookQuickShareFilter(classLoader: ClassLoader) {
        val pmClass =
            runCatching {
                classLoader.loadClass("android.app.ApplicationPackageManager")
            }.getOrNull() ?: run {
                log(Log.WARN, TAG, "Quick Share hook: ApplicationPackageManager not found")
                return
            }

        val methods = pmClass.declaredMethods.filter { it.name == "queryIntentActivitiesAsUser" }
        if (methods.isEmpty()) {
            log(Log.WARN, TAG, "Quick Share hook: no queryIntentActivitiesAsUser methods found")
            return
        }

        methods.forEach { method ->
            runCatching {
                method.isAccessible = true
                hook(method).intercept { chain ->
                    val result = chain.proceed()
                    val prefs = getRemotePreferences(PREFS_FILE_NAME)
                    val enabled = prefs?.getBoolean(PREF_KEY_HIDE_QUICK_SHARE, false) ?: false
                    if (!enabled) return@intercept result
                    val list = result as? MutableList<ResolveInfo> ?: return@intercept result
                    list.removeAll { it.activityInfo?.name == QUICK_SHARE_ACTIVITY }
                    result
                }
            }
        }
        log(Log.INFO, TAG, "Hooked queryIntentActivitiesAsUser (${methods.size} overloads)")
    }
}
