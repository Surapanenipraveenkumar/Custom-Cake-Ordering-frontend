package com.simats.cakeordering.model

import com.google.gson.annotations.SerializedName

data class CustomerMessagesResponse(
    val status: String,
    val customers: List<MessageCustomer>?,
    val bakers: List<BakerChat>?
)

data class MessageCustomer(
    val user_id: Int,
    val name: String,
    val profile_image: String?,
    val last_message: String?,
    val last_message_time: String,
    val time_ago: String
)

data class BakerChat(
    @SerializedName("baker_id")
    val bakerId: Int,
    @SerializedName("shop_name")
    val shopName: String?,
    @SerializedName("shop_image")
    val shopImage: String?,
    @SerializedName("last_message")
    val lastMessage: String?,
    @SerializedName("last_message_time")
    val lastMessageTime: String?
)
