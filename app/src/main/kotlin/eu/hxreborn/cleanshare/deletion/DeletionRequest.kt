package eu.hxreborn.cleanshare.deletion

internal data class DeletionRequest(
    val id: String,
    val uri: String,
    val filePath: String?,
    val filename: String,
    val createdAt: Long,
    val scheduledAt: Long,
    val status: RequestStatus,
)

internal enum class RequestStatus {
    PENDING,
    EXECUTING,
    COMPLETED,
    FAILED,
    CANCELED,
}
