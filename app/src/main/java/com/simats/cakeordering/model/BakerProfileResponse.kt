package com.simats.cakeordering.model

data class BakerProfileResponse(
    val status: String,
    val baker: BakerProfile?
)

data class BakerProfile(
    val baker_id: Int,
    val shop_name: String,
    val owner_name: String,
    val email: String,
    val phone: String,
    val address: String,
    val description: String,
    val profile_image: String?,
    val specialty: String?,
    val years_experience: Int?,
    val latitude: Double?,
    val longitude: Double?,
    val is_online: Boolean?,
    val total_orders: Int,
    val rating: Double,
    val monthly_income: Double
)
