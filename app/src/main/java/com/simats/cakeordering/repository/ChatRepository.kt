package com.simats.cakeordering.repository

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import com.simats.cakeordering.model.LocalChatMessage
import org.json.JSONArray
import org.json.JSONObject

/**
 * Chat storage with SharedPreferences persistence
 * Messages are saved locally and persist when app closes
 */
object ChatRepository {
    
    private const val PREFS_NAME = "ChatMessages"
    private lateinit var prefs: SharedPreferences
    
    // In-memory cache of conversations
    private val conversations = mutableMapOf<String, MutableList<LocalChatMessage>>()
    
    // Listeners for real-time updates
    private val listeners = mutableMapOf<String, MutableList<(List<LocalChatMessage>) -> Unit>>()
    
    /**
     * Initialize with context - call this in Application or first Activity
     */
    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        loadAllConversations()
    }
    
    /**
     * Load all saved conversations from SharedPreferences
     */
    private fun loadAllConversations() {
        val allEntries = prefs.all
        for ((key, value) in allEntries) {
            if (value is String) {
                try {
                    val messages = deserializeMessages(value)
                    conversations[key] = messages.toMutableList()
                } catch (e: Exception) {
                    // Ignore invalid entries
                }
            }
        }
    }
    
    /**
     * Get conversation key for a baker-user pair
     */
    private fun getKey(bakerId: Int, userId: Int): String = "${bakerId}_${userId}"
    
    /**
     * Get all messages for a conversation
     */
    fun getMessages(bakerId: Int, userId: Int): List<LocalChatMessage> {
        val key = getKey(bakerId, userId)
        return conversations[key]?.toList() ?: emptyList()
    }
    
    /**
     * Send a text message
     */
    fun sendMessage(bakerId: Int, userId: Int, senderType: String, message: String) {
        val key = getKey(bakerId, userId)
        val chatMessage = LocalChatMessage(
            senderType = senderType,
            message = message,
            imageUri = null
        )
        addMessage(key, chatMessage)
    }
    
    /**
     * Send an image message
     */
    fun sendImage(bakerId: Int, userId: Int, senderType: String, imageUri: Uri, caption: String? = null) {
        val key = getKey(bakerId, userId)
        val chatMessage = LocalChatMessage(
            senderType = senderType,
            message = caption,
            imageUri = imageUri
        )
        addMessage(key, chatMessage)
    }
    
    /**
     * Send message with both text and image
     */
    fun sendMessageWithImage(bakerId: Int, userId: Int, senderType: String, message: String?, imageUri: Uri?) {
        val key = getKey(bakerId, userId)
        val chatMessage = LocalChatMessage(
            senderType = senderType,
            message = message,
            imageUri = imageUri
        )
        addMessage(key, chatMessage)
    }
    
    private fun addMessage(key: String, message: LocalChatMessage) {
        if (!conversations.containsKey(key)) {
            conversations[key] = mutableListOf()
        }
        conversations[key]?.add(message)
        
        // Save to SharedPreferences
        saveConversation(key)
        
        // Notify listeners
        listeners[key]?.forEach { listener ->
            listener(conversations[key]?.toList() ?: emptyList())
        }
    }
    
    /**
     * Save a conversation to SharedPreferences
     */
    private fun saveConversation(key: String) {
        val messages = conversations[key] ?: return
        val json = serializeMessages(messages)
        prefs.edit().putString(key, json).apply()
    }
    
    /**
     * Serialize messages to JSON string
     */
    private fun serializeMessages(messages: List<LocalChatMessage>): String {
        val jsonArray = JSONArray()
        for (msg in messages) {
            val jsonObj = JSONObject()
            jsonObj.put("id", msg.id)
            jsonObj.put("senderType", msg.senderType)
            jsonObj.put("message", msg.message ?: "")
            jsonObj.put("imageUri", msg.imageUri?.toString() ?: "")
            jsonObj.put("timestamp", msg.timestamp)
            jsonArray.put(jsonObj)
        }
        return jsonArray.toString()
    }
    
    /**
     * Deserialize messages from JSON string
     */
    private fun deserializeMessages(json: String): List<LocalChatMessage> {
        val messages = mutableListOf<LocalChatMessage>()
        val jsonArray = JSONArray(json)
        for (i in 0 until jsonArray.length()) {
            val jsonObj = jsonArray.getJSONObject(i)
            val imageUriStr = jsonObj.optString("imageUri", "")
            val message = LocalChatMessage(
                id = jsonObj.getLong("id"),
                senderType = jsonObj.getString("senderType"),
                message = jsonObj.optString("message", "").ifEmpty { null },
                imageUri = if (imageUriStr.isNotEmpty()) Uri.parse(imageUriStr) else null,
                timestamp = jsonObj.getLong("timestamp")
            )
            messages.add(message)
        }
        return messages
    }
    
    /**
     * Register a listener for message updates
     */
    fun addListener(bakerId: Int, userId: Int, listener: (List<LocalChatMessage>) -> Unit) {
        val key = getKey(bakerId, userId)
        if (!listeners.containsKey(key)) {
            listeners[key] = mutableListOf()
        }
        listeners[key]?.add(listener)
    }
    
    /**
     * Remove a listener
     */
    fun removeListener(bakerId: Int, userId: Int, listener: (List<LocalChatMessage>) -> Unit) {
        val key = getKey(bakerId, userId)
        listeners[key]?.remove(listener)
    }
    
    /**
     * Clear all conversations
     */
    fun clearAll() {
        conversations.clear()
        prefs.edit().clear().apply()
    }
    
    /**
     * Delete a specific conversation
     */
    fun deleteConversation(bakerId: Int, userId: Int) {
        val key = getKey(bakerId, userId)
        conversations.remove(key)
        prefs.edit().remove(key).apply()
    }
}
