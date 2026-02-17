package eu.hxreborn.cleanshare

import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ShortcutManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.UserHandle
import android.widget.Toast
import androidx.core.net.toUri
import eu.hxreborn.cleanshare.hook.deletion.CheckboxHook
import eu.hxreborn.cleanshare.hook.deletion.DeletionHook
import eu.hxreborn.cleanshare.hook.directshare.LowRamHooker
import eu.hxreborn.cleanshare.hook.directshare.ShareTargetsHooker
import eu.hxreborn.cleanshare.hook.quickshare.QuickShareFilterHooker
import eu.hxreborn.cleanshare.util.ACTION_DELETE_SCREENSHOT
import eu.hxreborn.cleanshare.util.findClass
import eu.hxreborn.cleanshare.util.findMethodByName
import io.github.libxposed.api.XposedInterface
import io.github.libxposed.api.XposedModule
import io.github.libxposed.api.XposedModuleInterface.ModuleLoadedParam
import io.github.libxposed.api.XposedModuleInterface.PackageLoadedParam
import io.github.libxposed.api.XposedModuleInterface.SystemServerLoadedParam

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

    override fun onSystemServerLoaded(param: SystemServerLoadedParam) {
        super.onSystemServerLoaded(param)
        registerDeletionReceiver(param.classLoader)
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

    private fun isValidDeletionUri(uri: Uri): Boolean {
        if (uri.scheme != "content") return false
        val authority = uri.authority ?: return false
        val allowedAuthorities =
            setOf(
                "media",
                "com.android.providers.media",
                "com.android.providers.media.documents",
            )
        return authority in allowedAuthorities || authority.startsWith("media")
    }

    private fun registerDeletionReceiver(classLoader: ClassLoader) {
        runCatching {
            val activityThreadClass = classLoader.loadClass("android.app.ActivityThread")
            val currentThread = activityThreadClass.getMethod("currentActivityThread").invoke(null)
            val context =
                activityThreadClass.getMethod("getSystemContext").invoke(currentThread) as Context

            val handler = Handler(Looper.getMainLooper())

            val receiver =
                object : BroadcastReceiver() {
                    override fun onReceive(
                        ctx: Context,
                        intent: Intent,
                    ) {
                        val uriString = intent.getStringExtra("uri") ?: return
                        val filename = intent.getStringExtra("filename") ?: "Screenshot"
                        val delayMs = intent.getLongExtra("delay_ms", 15_000L)

                        val uri = uriString.toUri()
                        if (!isValidDeletionUri(uri)) {
                            log("Deletion rejected: invalid URI $uri")
                            return
                        }

                        val safeDelay = delayMs.coerceIn(0L, 60_000L)
                        handler.postDelayed(
                            {
                                runCatching {
                                    val rows = ctx.contentResolver.delete(uri, null, null)
                                    log("Deleted $uri ($rows rows)")

                                    if (rows > 0) {
                                        Toast
                                            .makeText(
                                                ctx,
                                                "Deleted: $filename",
                                                Toast.LENGTH_SHORT,
                                            ).show()
                                    }
                                }.onFailure { log("Deletion failed for $uri", it) }
                            },
                            safeDelay,
                        )
                    }
                }

            val filter = IntentFilter(ACTION_DELETE_SCREENSHOT)
            handler.post {
                runCatching {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        context.registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED)
                    } else {
                        @Suppress("UnspecifiedRegisterReceiverFlag")
                        context.registerReceiver(
                            receiver,
                            filter,
                        )
                    }
                    log("Deletion receiver registered")
                }.onFailure { log("Receiver registration failed", it) }
            }
        }.onFailure { log("Failed to set up deletion receiver", it) }
    }
}
