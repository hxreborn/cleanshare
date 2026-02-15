package eu.hxreborn.cleanshare.hook.deletion

import android.annotation.SuppressLint
import android.app.Activity
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import eu.hxreborn.cleanshare.util.CHECKBOX_MARGIN_END_DP
import eu.hxreborn.cleanshare.util.CHECKBOX_MARGIN_TOP_DP
import eu.hxreborn.cleanshare.util.CHECKBOX_VIEW_TAG
import eu.hxreborn.cleanshare.util.HEADLINE_ROW_HEIGHT_DP
import eu.hxreborn.cleanshare.util.debugLog

internal object CheckboxInserter {
    // Tries insertion strategies in order of preference:
    // 1. Headline row (A13+) - inline with "Sharing image" text
    // 2. Below preview (A11-12) - centered between preview and app icons
    fun insert(
        activity: Activity,
        view: View,
    ): Boolean = insertIntoHeadlineRow(activity, view) || insertBelowPreview(activity, view)

    // IntentResolver uses both android:id and com.android.intentresolver:id namespaces
    @SuppressLint("DiscouragedApi")
    private fun findViewId(
        activity: Activity,
        name: String,
    ): Int {
        val packages = listOf("android", activity.packageName)
        for (pkg in packages) {
            val id = activity.resources.getIdentifier(name, "id", pkg)
            if (id != 0) return id
        }
        return 0
    }

    // Insert into chooser_headline_row_container FrameLayout, right-aligned (A13+).
    // https://cs.android.com/android/platform/superproject/+/master:packages/modules/IntentResolver/java/res/layout/chooser_headline_row.xml
    private fun insertIntoHeadlineRow(
        activity: Activity,
        view: View,
    ): Boolean = runCatching {
        val id = findViewId(activity, "chooser_headline_row_container")
        if (id == 0) return@runCatching false

        val container = activity.findViewById<FrameLayout>(id) ?: return@runCatching false
        if (container.findViewWithTag<View>(CHECKBOX_VIEW_TAG) != null) return@runCatching true

        val density = activity.resources.displayMetrics.density
        val params = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                (HEADLINE_ROW_HEIGHT_DP * density).toInt(),
            ).apply {
                gravity = Gravity.END or Gravity.TOP
                marginEnd = (CHECKBOX_MARGIN_END_DP * density).toInt()
            }
        view.layoutParams = params
        container.addView(view)
        debugLog { "insertIntoHeadlineRow: added to chooser_headline_row_container" }
        true
    }.onFailure { debugLog(it) { "insertIntoHeadlineRow failed" } }.getOrDefault(false)

    // Insert into chooser_header after "Share" title (A11-12).
    // https://cs.android.com/android/platform/superproject/+/android-11.0.0_r1:frameworks/base/core/res/res/layout/chooser_grid.xml;l=32
    private fun insertBelowPreview(
        activity: Activity,
        view: View,
    ): Boolean = runCatching {
        val id = findViewId(activity, "chooser_header")
        if (id == 0) return@runCatching false

        val header = activity.findViewById<ViewGroup>(id) ?: return@runCatching false
        if (header.findViewWithTag<View>(CHECKBOX_VIEW_TAG) != null) return@runCatching true

        val density = activity.resources.displayMetrics.density
        view.tag = CHECKBOX_VIEW_TAG
        view.layoutParams = ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            ).apply {
                topMargin = (CHECKBOX_MARGIN_TOP_DP * density).toInt()
            }
        header.addView(view)
        debugLog { "insertBelowPreview: added to chooser_header" }
        true
    }.onFailure { debugLog(it) { "insertBelowPreview failed" } }.getOrDefault(false)
}
