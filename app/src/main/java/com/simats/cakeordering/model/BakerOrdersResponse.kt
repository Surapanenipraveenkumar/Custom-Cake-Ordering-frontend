package com.simats.cakeordering.model

data class BakerOrdersResponse(
    val status: String,
    val orders: List<BakerOrder>
)

data class BakerOrder(
    val order_id: Int,
    val order_id_str: String? = null,
    val cake_id: Int,
    val cake_name: String,
    val cake_image: String?,
    val customer_name: String,
    val customer_email: String? = null,
    val price: Double,
    val quantity: Int,
    val order_date: String,
    val delivery_type: String,
    val delivery_address: String? = null,
    val delivery_date: String? = null,
    val delivery_time: String? = null,
    val payment_method: String? = null,
    val status: String,
    val total_amount: Double? = null,
    val custom_options: String? = null
)
