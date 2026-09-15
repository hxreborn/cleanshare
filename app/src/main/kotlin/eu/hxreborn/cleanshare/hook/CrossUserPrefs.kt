package eu.hxreborn.cleanshare.hook

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.os.Process
import eu.hxreborn.cleanshare.util.PER_USER_RANGE
import eu.hxreborn.cleanshare.util.QUERY_PROVIDER_AUTHORITY
import eu.hxreborn.cleanshare.util.log
import android.content.SharedPreferences.OnSharedPreferenceChangeListener as OnChangeListener

private val OWNER_QUERY_URI = Uri.parse("content://0@$QUERY_PROVIDER_AUTHORITY")

// LSPosed keys module prefs by the calling process user
internal fun refreshCrossUserPrefs(context: Context) {
    val user = Process.myUid() / PER_USER_RANGE
    if (user == 0) return
    runCatching {
        val result = context.contentResolver.call(OWNER_QUERY_URI, "prefs", null, null)
        val status = result?.getString("status")
        if (status != "ok") {
            log("skip cross-user prefs status=$status")
            return
        }
        loadHookPrefs(BundlePreferences(result))
        // Deletion queue, media lookup and root all live in the owner user
        deletionEnabled = false
        log("loaded cross-user prefs user=$user filtered=${filteredApps.size}")
    }.onFailure { log("cross-user prefs failed", it) }
}

private class BundlePreferences(
    private val values: Bundle,
) : SharedPreferences {
    override fun getAll(): Map<String, *> = throw UnsupportedOperationException()

    override fun getString(
        key: String,
        defValue: String?,
    ): String? = values.getString(key, defValue)

    override fun getStringSet(
        key: String,
        defValues: MutableSet<String>?,
    ): MutableSet<String>? = values.getStringArrayList(key)?.toMutableSet() ?: defValues

    override fun getInt(
        key: String,
        defValue: Int,
    ): Int = values.getInt(key, defValue)

    override fun getLong(
        key: String,
        defValue: Long,
    ): Long = values.getLong(key, defValue)

    override fun getFloat(
        key: String,
        defValue: Float,
    ): Float = values.getFloat(key, defValue)

    override fun getBoolean(
        key: String,
        defValue: Boolean,
    ): Boolean = values.getBoolean(key, defValue)

    override fun contains(key: String): Boolean = values.containsKey(key)

    override fun edit(): SharedPreferences.Editor = throw UnsupportedOperationException()

    override fun registerOnSharedPreferenceChangeListener(l: OnChangeListener?) = Unit

    override fun unregisterOnSharedPreferenceChangeListener(l: OnChangeListener?) = Unit
}
