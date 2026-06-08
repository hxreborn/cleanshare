package eu.hxreborn.cleanshare.hook.deletion

import android.annotation.SuppressLint
import android.app.Activity
import android.net.Uri
import android.widget.CheckBox
import eu.hxreborn.cleanshare.hook.deletionEnabled
import eu.hxreborn.cleanshare.hook.deletionMode
import eu.hxreborn.cleanshare.hook.screenshotPattern
import eu.hxreborn.cleanshare.prefs.DeletionMode
import eu.hxreborn.cleanshare.util.CHECKBOX_INSERT_RETRY_DELAY_MS
import eu.hxreborn.cleanshare.util.CHECKBOX_TEXT_SIZE_SP
import eu.hxreborn.cleanshare.util.CHECKBOX_VIEW_TAG
import eu.hxreborn.cleanshare.util.debugLog
import java.util.concurrent.Executors

internal object CheckboxHook {
    // Single-thread executor for MediaStore IPC. Never block the interceptor thread:
    // the share-sheet UI thread is the caller, and IPC there causes ANR.
    private val executor = Executors.newSingleThreadExecutor()

    @Volatile private var defaultDeleteAfterShare: Boolean = false

    @SuppressLint("NewApi")
    fun onChooserCreated(activity: Activity) {
        debugLog { "onCreate hook fired" }
        ShareState.clear()
        val rawIntent = activity.intent ?: return
        debugLog { "rawIntent action=${rawIntent.action}" }

        val shareIntent = extractShareIntent(rawIntent) ?: return
        val uri = extractImageUri(shareIntent) ?: return

        if (!deletionEnabled) return

        val mode = deletionMode

        val screenshotInfo = getScreenshotInfo(activity, uri, screenshotPattern)
        if (screenshotInfo != null) {
            val targetUri = screenshotInfo.resolvedUri ?: uri
            applyDeletionMode(activity, mode, targetUri, screenshotInfo)
            return
        }

        if (!isOriginalFileUri(uri)) {
            executor.execute {
                val resolved =
                    resolveOriginalScreenshot(activity, screenshotPattern) ?: return@execute
                val targetUri = resolved.resolvedUri ?: uri
                activity.runOnUiThread {
                    applyDeletionMode(activity, mode, targetUri, resolved)
                }
            }
        }
    }

    private fun applyDeletionMode(
        activity: Activity,
        mode: DeletionMode,
        uri: Uri,
        info: ScreenshotInfo,
    ) {
        when (mode) {
            DeletionMode.ALWAYS -> {
                ShareState.set(uri, info.filename, info.filePath, shouldDelete = true, activity)
            }

            DeletionMode.ASK_EACH_TIME -> {
                activity.window.decorView.post { insertCheckbox(activity, uri, info) }
            }
        }
    }

    private fun insertCheckbox(
        activity: Activity,
        uri: Uri,
        screenshotInfo: ScreenshotInfo,
    ) {
        runCatching {
            val checked = defaultDeleteAfterShare
            val isResolved = screenshotInfo.resolvedUri != null
            val label = if (isResolved) "Delete original screenshot" else "Delete after sharing"

            val checkBox =
                CheckBox(activity).apply {
                    text = label
                    tag = CHECKBOX_VIEW_TAG
                    textSize = CHECKBOX_TEXT_SIZE_SP
                    isChecked = checked
                    setOnCheckedChangeListener { _, isChecked ->
                        ShareState.updateShouldDelete(isChecked)
                        defaultDeleteAfterShare = isChecked
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
