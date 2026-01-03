package com.simats.cakeordering.model

import com.google.gson.annotations.SerializedName

data class PlaceOrderResponse(
    val status: String,
    val message: String,
    @SerializedName("order_id")
    val orderId: Int?,
    @SerializedName("order_id_str")
    val orderIdStr: String?,
    val subtotal: Double?,
    @SerializedName("delivery_fee")
    val deliveryFee: Int?,
    @SerializedName("total_amount")
    val totalAmount: Double?,
    val items: List<OrderItemInfo>?
)

data class OrderItemInfo(
    @SerializedName("cake_id")
    val cakeId: Int,
    @SerializedName("cake_name")
    val cakeName: String,
    val price: Double,
    val quantity: Int,
    val image: String?,
    @SerializedName("item_total")
    val itemTotal: Double
)
