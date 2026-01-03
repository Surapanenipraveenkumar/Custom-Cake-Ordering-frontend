package com.simats.cakeordering.model

data class BakerDashboardResponse(
    val status: String,
    val message: String,
    val monthlyIncome: Double,
    val totalOrders: Int,
    val pendingOrders: Int
)
