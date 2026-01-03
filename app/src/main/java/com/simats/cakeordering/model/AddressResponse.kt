package com.simats.cakeordering.model

import com.google.gson.annotations.SerializedName

data class AddressResponse(
    val status: String,
    val addresses: List<Address>
)

data class Address(
    @SerializedName("address_id")
    val addressId: Int,
    @SerializedName("user_id")
    val userId: Int,
    val label: String,
    @SerializedName("full_address")
    val fullAddress: String,
    val pincode: String?,
    val landmark: String?,
    val phone: String?,
    @SerializedName("is_default")
    val isDefault: Int
)
