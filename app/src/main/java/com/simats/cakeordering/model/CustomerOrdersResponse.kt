package com.simats.cakeordering.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class CustomerOrdersResponse(
    val status: String,
    @SerializedName("total_orders")
    val totalOrders: Int,
    val stats: OrderStatusStats?,
    val orders: List<CustomerOrder>
)

data class OrderStatusStats(
    val pending: Int,
    @SerializedName("in_progress")
    val inProgress: Int,
    val delivered: Int,
    val cancelled: Int
)

data class CustomerOrder(
    @SerializedName("order_id")
    val orderId: Int,
    @SerializedName("order_id_str")
    val orderIdStr: String,
    @SerializedName("baker_name")
    val bakerName: String,
    @SerializedName("cake_name")
    val cakeName: String,
    @SerializedName("cake_image")
    val cakeImage: String?,
    @SerializedName("total_amount")
    val totalAmount: Double,
    val status: String,
    @SerializedName("order_date")
    val orderDate: String,
    @SerializedName("order_time")
    val orderTime: String?,
    @SerializedName("payment_method")
    val paymentMethod: String?,
    @SerializedName("delivery_address")
    val deliveryAddress: String?
) : Serializable

