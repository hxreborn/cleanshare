package eu.hxreborn.cleanshare.deletion

import android.util.AtomicFile
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

internal class DeletionQueue(
    filesDir: File,
) {
    private val file = AtomicFile(File(filesDir, "deletion_queue.json"))
    private val lock = Any()
    private val requests = mutableListOf<DeletionRequest>()

    init {
        load()
        clearStaleRequests()
    }

    private fun load() {
        synchronized(lock) {
            runCatching {
                val bytes = file.readFully()
                val array = JSONArray(String(bytes))
                repeat(array.length()) { i ->
                    val obj = array.getJSONObject(i)
                    requests +=
                        DeletionRequest(
                            id = obj.getString("id"),
                            uri = obj.getString("uri"),
                            filePath = obj.optString("filePath").takeIf { it.isNotEmpty() },
                            filename = obj.getString("filename"),
                            createdAt = obj.getLong("createdAt"),
                            scheduledAt = obj.getLong("scheduledAt"),
                            status = RequestStatus.valueOf(obj.getString("status")),
                        )
                }
            }.onFailure {
                Log.d(TAG, "Queue load failed: ${it.message}")
            }
        }
    }

    private fun clearStaleRequests() {
        synchronized(lock) {
            val before = requests.size
            // EXECUTING requests are stale (process died mid-execution)
            // Very old PENDING/COMPLETED/FAILED can be cleaned up
            val staleThreshold = System.currentTimeMillis() - STALE_AGE_MS
            requests.removeAll {
                it.status == RequestStatus.EXECUTING ||
                    it.status == RequestStatus.COMPLETED ||
                    it.status == RequestStatus.FAILED ||
                    (it.status == RequestStatus.PENDING && it.scheduledAt < staleThreshold)
            }
            val removed = before - requests.size
            if (removed > 0) {
                persist()
                Log.d(TAG, "Cleared $removed stale requests")
            }
        }
    }

    fun getPendingRequests(): List<DeletionRequest> {
        synchronized(lock) {
            return requests.filter { it.status == RequestStatus.PENDING }
        }
    }

    fun enqueue(request: DeletionRequest) {
        synchronized(lock) {
            requests.removeAll { it.uri == request.uri }
            requests += request
            persist()
        }
        val delay = request.scheduledAt - request.createdAt
        Log.d(TAG, "Enqueued: ${request.uri.substringAfterLast('/')}, delay=${delay}ms")
    }

    fun updateStatus(
        id: String,
        status: RequestStatus,
    ) {
        synchronized(lock) {
            val idx = requests.indexOfFirst { it.id == id }
            if (idx >= 0) {
                requests[idx] = requests[idx].copy(status = status)
                persist()
            }
        }
    }

    fun cancel(uri: String): Boolean {
        synchronized(lock) {
            val removed = requests.removeAll { it.uri == uri && it.status == RequestStatus.PENDING }
            if (removed) persist()
            return removed
        }
    }

    private fun persist() {
        runCatching {
            val array = JSONArray()
            requests.forEach { req ->
                array.put(
                    JSONObject().apply {
                        put("id", req.id)
                        put("uri", req.uri)
                        put("filePath", req.filePath)
                        put("filename", req.filename)
                        put("createdAt", req.createdAt)
                        put("scheduledAt", req.scheduledAt)
                        put("status", req.status.name)
                    },
                )
            }
            val bytes = array.toString().toByteArray()
            val stream = file.startWrite()
            try {
                stream.write(bytes)
                file.finishWrite(stream)
            } catch (e: Exception) {
                file.failWrite(stream)
                throw e
            }
        }.onFailure {
            Log.d(TAG, "Queue persist failed: ${it.message}")
        }
    }

    companion object {
        private const val TAG = "CleanShare"
        private const val STALE_AGE_MS = 60 * 60 * 1000L // 1 hour
    }
}
