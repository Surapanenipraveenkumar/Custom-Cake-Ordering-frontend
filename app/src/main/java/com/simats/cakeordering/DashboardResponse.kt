package com.simats.cakeordering
data class DashboardResponse(
    val status: String,
    val stats: Stats,
    val cakes: List<Cake>,
    val recent_orders: List<RecentOrder>
)

data class Stats(
    val monthly_income: Int,
    val total_orders: Int,
    val pending_orders: Int
)

data class Cake(
    val cake_id: Int,
    val cake_name: String,
    val price: Int,
    val image: String,
    val orders: Int
)

data class RecentOrder(
    val customer_name: String,
    val cake_name: String,
    val amount: Int,
    val status: String,
    val time: String
)
