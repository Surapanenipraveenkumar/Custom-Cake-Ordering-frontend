package com.simats.cakeordering.model

import com.google.gson.annotations.SerializedName

data class NotificationsResponse(
    val status: String,
    @SerializedName("unread_count")
    val unreadCount: Int,
    val notifications: List<NotificationItem>
)

data class NotificationItem(
    @SerializedName("notification_id")
    val notificationId: Int,
    val type: String,
    val title: String,
    val message: String?,
    @SerializedName("order_id")
    val orderId: Int?,
    @SerializedName("is_read")
    val isRead: Boolean,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("time_ago")
    val timeAgo: String
)
