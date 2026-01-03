package com.simats.cakeordering.model

import com.google.gson.annotations.SerializedName

data class DeliveryOrdersResponse(
    val status: String?,
    val message: String?,
    val orders: List<DeliveryOrder>?
)
