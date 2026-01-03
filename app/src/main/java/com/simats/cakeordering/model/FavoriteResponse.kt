package com.simats.cakeordering.model

import com.google.gson.annotations.SerializedName

data class FavoriteResponse(
    val status: String,
    val message: String?,
    @SerializedName("is_favorite")
    val isFavorite: Boolean?
)

data class FavoritesListResponse(
    val status: String,
    val favorites: List<FavoriteCake>?,
    val count: Int?
)

data class FavoriteCake(
    @SerializedName("cake_id")
    val cakeId: Int,
    @SerializedName("cake_name")
    val cakeName: String?,
    val price: Double,
    val image: String?,
    val description: String?,
    @SerializedName("baker_id")
    val bakerId: Int,
    @SerializedName("baker_name")
    val bakerName: String?,
    @SerializedName("favorited_at")
    val favoritedAt: String?
)
