package eu.hxreborn.cleanshare.hook

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.os.Process
import android.os.UserHandle
import android.os.UserManager
import eu.hxreborn.cleanshare.prefs.Prefs
import eu.hxreborn.cleanshare.util.PER_USER_RANGE
import eu.hxreborn.cleanshare.util.QUERY_PROVIDER_AUTHORITY
import eu.hxreborn.cleanshare.util.log
import android.content.SharedPreferences.OnSharedPreferenceChangeListener as OnChangeListener

// LSPosed keys module prefs by the calling process user
internal fun refreshCrossUserPrefs(
    context: Context,
    ownPrefs: SharedPreferences?,
) {
    val user = Process.myUid() / PER_USER_RANGE
    val others =
        runCatching {
            val getIdentifier = UserHandle::class.java.getMethod("getIdentifier")
            context.getSystemService(UserManager::class.java).userProfiles.map {
                getIdentifier.invoke(it) as Int
            }
        }.getOrDefault(listOf(0)).filter { it != user }
    if (others.isEmpty()) return
    val remote = others.associateWith { fetchPrefs(context, it) }
    val missing = remote.filterValues { it == null }.keys
    val base = if (user == 0) ownPrefs else remote[0]
    if (base == null) {
        log("skip cross-user prefs user=$user missing=$missing")
        return
    }
    loadHookPrefs(base)
    // Deletion queue, media lookup and root all live in the owner user
    if (user != 0) deletionEnabled = false
    filteredApps =
        (remote.values + ownPrefs)
            .filterNotNull()
            .flatMapTo(filteredApps.toHashSet()) { Prefs.FILTERED_APPS.read(it) }
    log("loaded cross-user prefs user=$user missing=$missing filtered=${filteredApps.size}")
}

private fun fetchPrefs(
    context: Context,
    user: Int,
): SharedPreferences? =
    runCatching {
        val uri = Uri.parse("content://$user@$QUERY_PROVIDER_AUTHORITY")
        context.contentResolver
            .call(uri, "prefs", null, null)
            ?.takeIf { it.getString("status") == "ok" }
            ?.let(::BundlePreferences)
    }.onFailure { log("cross-user prefs failed user=$user", it) }.getOrNull()

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
