package com.simats.cakeordering.model

import com.google.gson.annotations.SerializedName

data class DeliveryProfileResponse(
    val status: String?,
    val message: String?,
    @SerializedName("delivery_id")
    val deliveryId: Int?,
    val name: String?,
    val email: String?,
    val phone: String?,
    val vehicle: String?,
    @SerializedName("is_online")
    val isOnline: Int?,
    @SerializedName("total_deliveries")
    val totalDeliveries: Int?,
    @SerializedName("total_earnings")
    val totalEarnings: Double?,
    @SerializedName("month_deliveries")
    val monthDeliveries: Int?,
    @SerializedName("month_earnings")
    val monthEarnings: Double?,
    val rating: Double?,
    @SerializedName("created_at")
    val createdAt: String?,
    @SerializedName("service_area")
    val serviceArea: String?,
    @SerializedName("vehicle_number")
    val vehicleNumber: String?
)
