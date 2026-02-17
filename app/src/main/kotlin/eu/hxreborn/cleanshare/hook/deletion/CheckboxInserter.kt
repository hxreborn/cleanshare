package eu.hxreborn.cleanshare.hook.deletion

import android.annotation.SuppressLint
import android.app.Activity
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import eu.hxreborn.cleanshare.util.CHECKBOX_MARGIN_TOP_DP
import eu.hxreborn.cleanshare.util.CHECKBOX_VIEW_TAG
import eu.hxreborn.cleanshare.util.debugLog

internal object CheckboxInserter {
    // Tries insertion strategies in order of preference:
    // 1. Preview content area (A11+) — centered below image preview / action row
    // 2. Headline row (A15+) — right-aligned inline with headline text
    // 3. Below header (A11+) — centered below chooser_header as last resort
    fun insert(
        activity: Activity,
        view: View,
    ): Boolean =
        insertIntoPreviewContent(activity, view) ||
            insertIntoHeadlineRow(activity, view) ||
            insertBelowHeader(activity, view)

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

    // Append to the inner LinearLayout inside content_preview_container (A11+).
    // The preview layout inflates a vertical LinearLayout as root on all versions:
    // A11-12: LinearLayout → RelativeLayout (images) + action row include
    // A13:    LinearLayout → CheckBox + image area + reselection + action ViewStub
    // A14:    LinearLayout → headline include + ScrollableImagePreviewView + action merge
    // A15-16: LinearLayout → headline ViewStub + ScrollableImagePreviewView + action merge
    //
    // AOSP refs:
    // A11: frameworks/base/+/android-11.0.0_r1/core/res/res/layout/chooser_grid_preview_image.xml
    // A13: IntentResolver/+/android13-qpr3-release/java/res/layout/chooser_grid_preview_image.xml
    // A14: IntentResolver/+/android14-release/java/res/layout/chooser_grid_preview_image.xml
    // A15: IntentResolver/+/android15-release/java/res/layout/chooser_grid_scrollable_preview.xml
    private fun insertIntoPreviewContent(
        activity: Activity,
        view: View,
    ): Boolean =
        runCatching {
            val id = findViewId(activity, "content_preview_container")
            if (id == 0) return@runCatching false

            val container = activity.findViewById<ViewGroup>(id) ?: return@runCatching false
            if (container.childCount == 0) return@runCatching false

            val inner = container.getChildAt(0) as? ViewGroup ?: return@runCatching false
            if (inner.findViewWithTag<View>(CHECKBOX_VIEW_TAG) != null) return@runCatching true

            val density = activity.resources.displayMetrics.density
            val params =
                LinearLayout
                    .LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                    ).apply {
                        gravity = Gravity.CENTER_HORIZONTAL
                        topMargin = (CHECKBOX_MARGIN_TOP_DP * density).toInt()
                    }
            view.layoutParams = params
            inner.addView(view)
            debugLog { "insertIntoPreviewContent: added to content_preview_container inner layout" }
            true
        }.onFailure { debugLog(it) { "insertIntoPreviewContent failed" } }.getOrDefault(false)

    // Insert right-aligned into chooser_headline_row_container FrameLayout (A15+).
    // AOSP: IntentResolver/+/android15-release/java/res/layout/chooser_grid_scrollable_preview.xml
    private fun insertIntoHeadlineRow(
        activity: Activity,
        view: View,
    ): Boolean =
        runCatching {
            val id = findViewId(activity, "chooser_headline_row_container")
            if (id == 0) return@runCatching false

            val container = activity.findViewById<FrameLayout>(id) ?: return@runCatching false
            if (container.findViewWithTag<View>(CHECKBOX_VIEW_TAG) != null) return@runCatching true

            val density = activity.resources.displayMetrics.density
            val marginEnd = (16 * density).toInt()
            val params =
                FrameLayout
                    .LayoutParams(
                        FrameLayout.LayoutParams.WRAP_CONTENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT,
                    ).apply {
                        gravity = Gravity.END or Gravity.CENTER_VERTICAL
                        this.marginEnd = marginEnd
                    }
            view.layoutParams = params
            container.addView(view)
            debugLog { "insertIntoHeadlineRow: added to chooser_headline_row_container" }
            true
        }.onFailure { debugLog(it) { "insertIntoHeadlineRow failed" } }.getOrDefault(false)

    // Insert centered below chooser_header (A11+, universal fallback).
    // AOSP ref: frameworks/base/+/android-11.0.0_r1/core/res/res/layout/chooser_grid.xml
    private fun insertBelowHeader(
        activity: Activity,
        view: View,
    ): Boolean =
        runCatching {
            val id = findViewId(activity, "chooser_header")
            if (id == 0) return@runCatching false

            val header = activity.findViewById<ViewGroup>(id) ?: return@runCatching false
            if (header.findViewWithTag<View>(CHECKBOX_VIEW_TAG) != null) return@runCatching true

            val density = activity.resources.displayMetrics.density
            val params =
                LinearLayout
                    .LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                    ).apply {
                        gravity = Gravity.CENTER_HORIZONTAL
                        topMargin = (CHECKBOX_MARGIN_TOP_DP * density).toInt()
                    }
            view.layoutParams = params
            header.addView(view)
            debugLog { "insertBelowHeader: added to chooser_header" }
            true
        }.onFailure { debugLog(it) { "insertBelowHeader failed" } }.getOrDefault(false)
}
