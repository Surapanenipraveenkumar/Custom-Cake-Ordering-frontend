package com.simats.cakeordering.model

import com.google.gson.annotations.SerializedName

data class NearbyBakersResponse(
    val status: String,
    val bakers: List<NearbyBaker>?,
    val count: Int?
)

data class NearbyBaker(
    @SerializedName("baker_id")
    val bakerId: Int,
    @SerializedName("shop_name")
    val shopName: String,
    @SerializedName("owner_name")
    val ownerName: String?,
    val address: String?,
    @SerializedName("profile_image")
    val profileImage: String?,
    val latitude: Double,
    val longitude: Double,
    val specialty: String?,
    @SerializedName("years_experience")
    val yearsExperience: Int,
    val distance: Double,
    val rating: Double,
    @SerializedName("review_count")
    val reviewCount: Int
)
