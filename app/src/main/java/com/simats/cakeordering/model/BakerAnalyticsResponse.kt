package com.simats.cakeordering.model

data class BakerAnalyticsResponse(
    val status: String,
    val today: DailyStats,
    val thisWeek: WeeklyStats,
    val thisMonth: MonthlyStats,
    val orderStats: OrderStats,
    val monthlyTrend: List<MonthTrend>,
    val topCakes: List<TopCake>,
    val ratings: RatingsData
)

data class DailyStats(
    val income: Int,
    val orders: Int
)

data class WeeklyStats(
    val income: Int,
    val orders: Int
)

data class MonthlyStats(
    val income: Int,
    val orders: Int,
    val percentChange: Double
)

data class OrderStats(
    val total: Int,
    val pending: Int,
    val completed: Int
)

data class MonthTrend(
    val month: String,
    val income: Int
)

data class TopCake(
    val cake_name: String,
    val order_count: Int,
    val revenue: Int
)

data class RatingsData(
    val average: Double,
    val total_reviews: Int,
    val distribution: List<RatingDistribution>
)

data class RatingDistribution(
    val stars: Int,
    val count: Int
)
