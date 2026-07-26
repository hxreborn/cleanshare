package eu.hxreborn.cleanshare

import android.app.Activity
import android.app.ActivityManager
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.pm.ResolveInfo
import android.content.pm.ShortcutManager
import android.os.Bundle
import android.util.Log
import eu.hxreborn.cleanshare.hook.deletion.CheckboxHook
import eu.hxreborn.cleanshare.hook.deletion.DeletionHook
import eu.hxreborn.cleanshare.hook.deletionDelayMs
import eu.hxreborn.cleanshare.hook.hideDirectShare
import eu.hxreborn.cleanshare.hook.hideQuickShare
import eu.hxreborn.cleanshare.hook.loadHookPrefs
import eu.hxreborn.cleanshare.util.PREFS_FILE_NAME
import eu.hxreborn.cleanshare.util.QUICK_SHARE_ACTIVITY
import eu.hxreborn.cleanshare.util.debugLog
import eu.hxreborn.cleanshare.util.findClass
import eu.hxreborn.cleanshare.util.findMethodByName
import io.github.libxposed.api.XposedModule
import io.github.libxposed.api.XposedModuleInterface.ModuleLoadedParam
import io.github.libxposed.api.XposedModuleInterface.PackageReadyParam

@PublishedApi
internal lateinit var module: CleanShareModule
    private set

private const val ANDROID_FRAMEWORK_PKG = "android"
private const val INTENT_RESOLVER_PKG = "com.android.intentresolver"
private const val AIAI_PKG = "com.google.android.as"

private val CHOOSER_CLASS_NAMES =
    listOf(
        "com.android.intentresolver.ChooserActivity",
        "com.android.internal.app.ChooserActivity",
    )

private val CHOOSER_ADAPTER_CLASS_NAMES =
    listOf(
        "com.android.intentresolver.ChooserListAdapter",
        "com.android.internal.app.ChooserListAdapter",
    )

class CleanShareModule : XposedModule() {
    companion object {
        const val TAG = "CleanShare"
    }

    private var remotePrefs: SharedPreferences? = null
    private var prefsListener: SharedPreferences.OnSharedPreferenceChangeListener? = null

    override fun onModuleLoaded(param: ModuleLoadedParam) {
        module = this
        log(
            Log.INFO,
            TAG,
            "loaded version=${BuildConfig.VERSION_NAME} process=${param.processName}",
        )
        remotePrefs =
            runCatching { getRemotePreferences(PREFS_FILE_NAME) }
                .onFailure { log(Log.WARN, TAG, "remote prefs unavailable", it) }
                .getOrNull()
        remotePrefs?.let { prefs ->
            runCatching { loadHookPrefs(prefs) }
                .onFailure { log(Log.WARN, TAG, "prefs load failed", it) }
            registerPrefsListener(prefs)
        }
    }

    private fun registerPrefsListener(prefs: SharedPreferences) {
        runCatching {
            val listener =
                SharedPreferences.OnSharedPreferenceChangeListener { sp, _ ->
                    runCatching { loadHookPrefs(sp) }
                        .onFailure { log(Log.WARN, TAG, "prefs reload failed", it) }
                }
            prefsListener = listener
            prefs.registerOnSharedPreferenceChangeListener(listener)
        }.onFailure { log(Log.WARN, TAG, "prefs listener failed", it) }
    }

    override fun onPackageReady(param: PackageReadyParam) {
        if (!param.isFirstPackage) return
        when (param.packageName) {
            // The unbundled chooser has android:enabled="false" on Android 13
            // https://android.googlesource.com/platform/packages/modules/IntentResolver/+/android13-qpr1-release/AndroidManifest.xml
            ANDROID_FRAMEWORK_PKG, INTENT_RESOLVER_PKG -> {
                hookLowRam()
                hookServiceTargetCountFallback(param.classLoader)
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
                debugLog { "intercept low-ram enabled=$hideDirectShare" }
                if (hideDirectShare) return@intercept true
                chain.proceed()
            }
            log(Log.INFO, TAG, "hooked low-ram")
        }.onFailure { log(Log.WARN, TAG, "hook low-ram failed", it) }
    }

    // Fallback for ROMs where ART inlines isLowRamDeviceStatic() into ChooserListAdapter
    private fun hookServiceTargetCountFallback(classLoader: ClassLoader) {
        val clazz =
            findClass(classLoader, CHOOSER_ADAPTER_CLASS_NAMES) ?: run {
                log(Log.WARN, TAG, "hook service-target-count failed reason=class-missing")
                return
            }
        runCatching {
            val method = clazz.getDeclaredMethod("getServiceTargetCount")
            method.isAccessible = true
            hook(method).intercept { chain ->
                debugLog { "intercept service-target-count enabled=$hideDirectShare" }
                if (hideDirectShare) return@intercept 0
                chain.proceed()
            }
            log(Log.INFO, TAG, "hooked service-target-count")
        }.onFailure { log(Log.WARN, TAG, "hook service-target-count failed", it) }
    }

    private fun hookScreenshotDelete(classLoader: ClassLoader) {
        val chooserClass =
            findClass(classLoader, CHOOSER_CLASS_NAMES) ?: run {
                log(Log.WARN, TAG, "hook chooser failed reason=class-missing")
                return
            }

        runCatching {
            val method = chooserClass.getDeclaredMethod("onCreate", Bundle::class.java)
            method.isAccessible = true
            hook(method).intercept { chain ->
                chain.proceed()
                CheckboxHook.onChooserCreated(
                    chain.thisObject as? Activity ?: return@intercept null,
                )
                null
            }
            log(Log.INFO, TAG, "hooked chooser-create")
        }.onFailure { log(Log.WARN, TAG, "hook chooser-create failed", it) }

        val startSelected =
            findMethodByName(chooserClass, "startSelected") ?: run {
                log(Log.WARN, TAG, "hook start-selected failed reason=method-missing")
                return
            }

        runCatching {
            startSelected.isAccessible = true
            hook(startSelected).intercept { chain ->
                chain.proceed()
                DeletionHook.onShareStarted(deletionDelayMs)
                null
            }
            log(Log.INFO, TAG, "hooked start-selected")
        }.onFailure { log(Log.WARN, TAG, "hook start-selected failed", it) }
    }

    private fun hookShareTargets() {
        runCatching {
            val method =
                ShortcutManager::class.java
                    .getDeclaredMethod("getShareTargets", IntentFilter::class.java)
            method.isAccessible = true
            hook(method).intercept { chain ->
                debugLog { "intercept share-targets enabled=$hideDirectShare" }
                if (hideDirectShare) return@intercept emptyList<Any>()
                chain.proceed()
            }
            log(Log.INFO, TAG, "hooked share-targets")
        }.onFailure { log(Log.WARN, TAG, "hook share-targets failed", it) }
    }

    @Suppress("UNCHECKED_CAST")
    private fun hookQuickShareFilter(classLoader: ClassLoader) {
        val pmClass =
            runCatching {
                classLoader.loadClass("android.app.ApplicationPackageManager")
            }.getOrNull() ?: run {
                log(Log.WARN, TAG, "hook quick-share failed reason=pm-class-missing")
                return
            }

        val methods = pmClass.declaredMethods.filter { it.name == "queryIntentActivitiesAsUser" }
        if (methods.isEmpty()) {
            log(Log.WARN, TAG, "hook quick-share failed reason=no-methods")
            return
        }

        val hooked =
            methods.count { method ->
                runCatching {
                    method.isAccessible = true
                    hook(method).intercept { chain ->
                        val result = chain.proceed()
                        if (!hideQuickShare) return@intercept result
                        val list = result as? MutableList<ResolveInfo> ?: return@intercept result
                        list.removeAll { it.activityInfo?.name == QUICK_SHARE_ACTIVITY }
                        result
                    }
                }.onFailure {
                    log(Log.ERROR, TAG, "hook quick-share overload failed", it)
                }.isSuccess
            }
        log(Log.INFO, TAG, "hooked quick-share overloads=$hooked/${methods.size}")
    }
}
