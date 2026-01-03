package com.simats.cakeordering.model

data class RecentOrder(
    val customer_name: String,
    val cake_name: String,
    val amount: Double,
    val status: String
)
