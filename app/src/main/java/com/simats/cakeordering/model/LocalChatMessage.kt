package com.simats.cakeordering.model

import android.net.Uri

/**
 * Local chat message - stored in memory only
 */
data class LocalChatMessage(
    val id: Long = System.currentTimeMillis(),
    val senderType: String,  // "baker" or "customer"
    val message: String?,
    val imageUri: Uri? = null,  // Local image URI (not URL)
    val timestamp: Long = System.currentTimeMillis()
) {
    fun getTimeString(): String {
        val sdf = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
        return sdf.format(java.util.Date(timestamp))
    }
}
