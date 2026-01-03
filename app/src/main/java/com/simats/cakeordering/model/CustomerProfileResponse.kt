package com.simats.cakeordering.model

import com.google.gson.annotations.SerializedName

data class CustomerProfileResponse(
    val status: String,
    val profile: CustomerProfile?,
    val stats: CustomerOrderStats?,
    @SerializedName("recent_orders")
    val recentOrders: List<ProfileOrder>?,
    @SerializedName("member_since")
    val memberSince: String?
)

data class CustomerProfile(
    @SerializedName("user_id")
    val userId: Int,
    val name: String,
    val email: String,
    val phone: String?,
    val address: String?
)

data class CustomerOrderStats(
    @SerializedName("total_orders")
    val totalOrders: Int,
    @SerializedName("pending_orders")
    val pendingOrders: Int,
    @SerializedName("delivered_orders")
    val deliveredOrders: Int
)

data class ProfileOrder(
    @SerializedName("order_id")
    val orderId: Int,
    @SerializedName("order_id_str")
    val orderIdStr: String?,
    @SerializedName("cake_name")
    val cakeName: String?,
    @SerializedName("cake_image")
    val cakeImage: String?,
    @SerializedName("total_amount")
    val totalAmount: Double,
    val status: String,
    @SerializedName("order_date")
    val orderDate: String?,
    @SerializedName("order_time")
    val orderTime: String?
)
