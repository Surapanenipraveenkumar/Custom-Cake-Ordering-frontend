package com.simats.cakeordering.model

data class ChatMessagesResponse(
    val status: String,
    val messages: List<ChatMessage>
)

data class ChatMessage(
    val message_id: Int,
    val sender_type: String,  // "customer" or "baker"
    val message: String?,
    val image_url: String?,
    val created_at: String,
    val time: String
)

data class SendMessageRequest(
    val baker_id: Int,
    val user_id: Int,
    val sender_type: String,
    val message: String?,
    val image_url: String?
)
