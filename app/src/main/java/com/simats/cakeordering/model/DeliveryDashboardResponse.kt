package com.simats.cakeordering.model

import com.google.gson.annotations.SerializedName

data class DeliveryDashboardResponse(
    val status: String?,
    val message: String?,
    @SerializedName("delivery_name")
    val deliveryName: String?,
    @SerializedName("is_online")
    val isOnline: Int?,
    @SerializedName("today_deliveries")
    val todayDeliveries: Int?,
    @SerializedName("today_earnings")
    val todayEarnings: Double?,
    @SerializedName("total_deliveries")
    val totalDeliveries: Int?,
    @SerializedName("pending_orders")
    val pendingOrders: List<DeliveryOrder>?
)

data class DeliveryOrder(
    @SerializedName("order_id")
    val orderId: Int,
    @SerializedName("customer_name")
    val customerName: String?,
    @SerializedName("customer_phone")
    val customerPhone: String?,
    @SerializedName("customer_address")
    val customerAddress: String?,
    @SerializedName("baker_name")
    val bakerName: String?,
    @SerializedName("baker_address")
    val bakerAddress: String?,
    @SerializedName("baker_phone")
    val bakerPhone: String?,
    @SerializedName("baker_lat")
    val bakerLat: Double?,
    @SerializedName("baker_lng")
    val bakerLng: Double?,
    @SerializedName("total_amount")
    val totalAmount: Double?,
    @SerializedName("delivery_status")
    val deliveryStatus: String?,
    @SerializedName("order_status")
    val orderStatus: String?,
    @SerializedName("created_at")
    val createdAt: String?,
    @SerializedName("picked_up_at")
    val pickedUpAt: String?,
    @SerializedName("delivered_at")
    val deliveredAt: String?,
    @SerializedName("is_assigned")
    val isAssigned: Boolean?
)
