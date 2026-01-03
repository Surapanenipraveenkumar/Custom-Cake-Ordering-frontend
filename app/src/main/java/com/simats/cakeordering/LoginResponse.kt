package com.simats.cakeordering.model

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    val status: String,
    val message: String? = null,
    @SerializedName("baker_id")
    val bakerId: Int? = null,
    @SerializedName("user_id")
    val userId: Int? = null,
    val name: String? = null
)
