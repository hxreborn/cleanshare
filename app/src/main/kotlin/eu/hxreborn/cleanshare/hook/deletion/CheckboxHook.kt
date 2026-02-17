package eu.hxreborn.cleanshare.hook.deletion

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.net.Uri
import android.widget.CheckBox
import androidx.core.content.edit
import eu.hxreborn.cleanshare.util.CHECKBOX_TEXT_SIZE_SP
import eu.hxreborn.cleanshare.util.CHECKBOX_VIEW_TAG
import eu.hxreborn.cleanshare.util.PREFS_FILE_NAME
import eu.hxreborn.cleanshare.util.PREF_KEY_DELETE_AFTER_SHARE
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

            val filename = getFilenameIfScreenshot(activity, uri) ?: return

            // Defer insertion since views aren't inflated yet during onCreate
            activity.window.decorView.post { insertCheckbox(activity, uri, filename) }
        }

        private fun insertCheckbox(
            activity: Activity,
            uri: Uri,
            filename: String,
        ) {
            runCatching {
                val checkBox = createCheckbox(activity)
                initializeShareState(uri, filename, checkBox.isChecked, activity)
                bindCheckboxToPrefs(checkBox, activity)

                val inserted = CheckboxInserter.insert(activity, checkBox)
                debugLog { "checkbox inserted=$inserted" }
            }.onFailure { debugLog(it) { "insertCheckbox failed" } }
        }

        @SuppressLint("SetTextI18n")
        private fun createCheckbox(activity: Activity): CheckBox {
            val prefs = activity.getSharedPreferences(PREFS_FILE_NAME, Context.MODE_PRIVATE)
            return CheckBox(activity).apply {
                text = "Delete after sharing"
                tag = CHECKBOX_VIEW_TAG
                textSize = CHECKBOX_TEXT_SIZE_SP
                isChecked = prefs.getBoolean(PREF_KEY_DELETE_AFTER_SHARE, false)
            }
        }

        private fun initializeShareState(
            uri: Uri,
            filename: String,
            checked: Boolean,
            activity: Activity,
        ) {
            ShareState.set(uri, filename, checked, activity)
        }

        private fun bindCheckboxToPrefs(
            checkBox: CheckBox,
            activity: Activity,
        ) {
            val prefs = activity.getSharedPreferences(PREFS_FILE_NAME, Context.MODE_PRIVATE)
            checkBox.setOnCheckedChangeListener { _, checked ->
                ShareState.updateShouldDelete(checked)
                prefs.edit { putBoolean(PREF_KEY_DELETE_AFTER_SHARE, checked) }
            }
        }
    }
}
