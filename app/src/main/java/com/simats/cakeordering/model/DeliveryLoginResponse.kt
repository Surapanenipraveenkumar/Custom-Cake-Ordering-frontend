package com.simats.cakeordering.model

import com.google.gson.annotations.SerializedName

data class DeliveryLoginResponse(
    val status: String?,
    val message: String?,
    @SerializedName("delivery_id")
    val deliveryId: Int?,
    val name: String?,
    val phone: String?,
    val vehicle: String?,
    val email: String?
)
