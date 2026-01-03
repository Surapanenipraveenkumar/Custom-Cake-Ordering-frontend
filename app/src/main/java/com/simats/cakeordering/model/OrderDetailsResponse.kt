package com.simats.cakeordering.model

import com.google.gson.annotations.SerializedName

data class OrderDetailsResponse(
    val status: String,
    val order: OrderDetails?
)

data class OrderDetails(
    @SerializedName("order_id")
    val orderId: Int,
    @SerializedName("order_id_str")
    val orderIdStr: String,
    val status: String,
    @SerializedName("order_date")
    val orderDate: String,
    @SerializedName("order_time")
    val orderTime: String?,
    @SerializedName("baker_name")
    val bakerName: String,
    @SerializedName("baker_phone")
    val bakerPhone: String?,
    @SerializedName("baker_address")
    val bakerAddress: String?,
    @SerializedName("customer_name")
    val customerName: String?,
    @SerializedName("customer_email")
    val customerEmail: String?,
    @SerializedName("customer_phone")
    val customerPhone: String?,
    @SerializedName("delivery_address")
    val deliveryAddress: String,
    @SerializedName("delivery_date")
    val deliveryDate: String?,
    @SerializedName("delivery_time")
    val deliveryTime: String?,
    @SerializedName("payment_method")
    val paymentMethod: String,
    val items: List<OrderItem>,
    val subtotal: Double,
    @SerializedName("delivery_fee")
    val deliveryFee: Double,
    @SerializedName("total_amount")
    val totalAmount: Double
)

data class OrderItem(
    @SerializedName("cake_id")
    val cakeId: Int,
    @SerializedName("cake_name")
    val cakeName: String,
    @SerializedName("cake_image")
    val cakeImage: String?,
    val quantity: Int,
    val price: Double,
    val customization: String?
)
