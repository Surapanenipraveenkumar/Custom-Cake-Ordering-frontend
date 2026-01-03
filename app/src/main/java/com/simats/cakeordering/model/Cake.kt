package com.simats.cakeordering.model

import com.google.gson.annotations.SerializedName

data class Cake(
    @SerializedName("cake_id")
    val cake_id: Int = 0,
    @SerializedName("baker_id")
    val baker_id: Int = 0,   // ✅ Baker ID for chat feature
    @SerializedName("cake_name", alternate = ["name"])
    val name: String? = null,
    @SerializedName("shop_name", alternate = ["baker"])
    val baker: String? = null,  // Baker/Shop name
    val price: String? = null,
    val image: String? = null
)
