package eu.hxreborn.cleanshare.hook.deletion

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.net.Uri
import android.widget.CheckBox
import androidx.core.content.edit
import eu.hxreborn.cleanshare.CleanShareModule
import eu.hxreborn.cleanshare.prefs.DeletionMode
import eu.hxreborn.cleanshare.util.CHECKBOX_INSERT_RETRY_DELAY_MS
import eu.hxreborn.cleanshare.util.CHECKBOX_TEXT_SIZE_SP
import eu.hxreborn.cleanshare.util.CHECKBOX_VIEW_TAG
import eu.hxreborn.cleanshare.util.PREFS_FILE_NAME
import eu.hxreborn.cleanshare.util.PREF_KEY_DELETE_AFTER_SHARE
import eu.hxreborn.cleanshare.util.PREF_KEY_DELETION_ENABLED
import eu.hxreborn.cleanshare.util.PREF_KEY_DELETION_MODE
import eu.hxreborn.cleanshare.util.debugLog
import io.github.libxposed.api.XposedInterface.AfterHookCallback
import io.github.libxposed.api.XposedInterface.Hooker
import io.github.libxposed.api.annotations.AfterInvocation
import io.github.libxposed.api.annotations.XposedHooker

@XposedHooker
class CheckboxHook : Hooker {
    companion object {
        @JvmStatic
        @AfterInvocation
        @SuppressLint("NewApi")
        fun after(callback: AfterHookCallback) {
            debugLog { "onCreate hook fired" }
            val activity = callback.thisObject as? Activity ?: return
            val rawIntent = activity.intent ?: return
            debugLog { "rawIntent action=${rawIntent.action}" }

            val shareIntent = extractShareIntent(rawIntent) ?: return
            val uri = extractImageUri(shareIntent) ?: return
            val screenshotInfo = getScreenshotInfo(activity, uri) ?: return

            val remotePrefs =
                runCatching {
                    CleanShareModule.instance?.getRemotePreferences(PREFS_FILE_NAME)
                }.getOrNull() ?: return

            val enabled = remotePrefs.getBoolean(PREF_KEY_DELETION_ENABLED, false)
            if (!enabled) return

            val modeKey =
                remotePrefs.getString(
                    PREF_KEY_DELETION_MODE,
                    DeletionMode.ASK_EACH_TIME.key,
                ) ?: DeletionMode.ASK_EACH_TIME.key
            val mode = DeletionMode.fromKey(modeKey)

            when (mode) {
                DeletionMode.ALWAYS -> {
                    ShareState.set(
                        uri,
                        screenshotInfo.filename,
                        screenshotInfo.filePath,
                        shouldDelete = true,
                        activity,
                    )
                }

                DeletionMode.ASK_EACH_TIME -> {
                    activity.window.decorView.post {
                        insertCheckbox(activity, uri, screenshotInfo)
                    }
                }
            }
        }

        private fun insertCheckbox(
            activity: Activity,
            uri: Uri,
            screenshotInfo: ScreenshotInfo,
        ) {
            runCatching {
                val localPrefs =
                    activity.getSharedPreferences(PREFS_FILE_NAME, Context.MODE_PRIVATE)
                val checked = localPrefs.getBoolean(PREF_KEY_DELETE_AFTER_SHARE, false)

                val checkBox =
                    CheckBox(activity).apply {
                        text = "Delete after sharing"
                        tag = CHECKBOX_VIEW_TAG
                        textSize = CHECKBOX_TEXT_SIZE_SP
                        isChecked = checked
                        setOnCheckedChangeListener { _, isChecked ->
                            ShareState.updateShouldDelete(isChecked)
                            localPrefs.edit {
                                putBoolean(PREF_KEY_DELETE_AFTER_SHARE, isChecked)
                            }
                        }
                    }

                ShareState.set(
                    uri,
                    screenshotInfo.filename,
                    screenshotInfo.filePath,
                    checked,
                    activity,
                )

                val inserted = CheckboxInserter.insert(activity, checkBox)
                debugLog { "checkbox inserted=$inserted" }

                if (!inserted) {
                    activity.window.decorView.postDelayed(
                        { retryInsert(activity, checkBox) },
                        CHECKBOX_INSERT_RETRY_DELAY_MS,
                    )
                }
            }.onFailure { debugLog(it) { "insertCheckbox failed" } }
        }

        private fun retryInsert(
            activity: Activity,
            checkBox: CheckBox,
        ) {
            runCatching {
                val inserted = CheckboxInserter.insert(activity, checkBox)
                debugLog { "checkbox retry inserted=$inserted" }
            }.onFailure { debugLog(it) { "retryInsert failed" } }
        }
    }
}
