package com.simats.cakeordering

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.simats.cakeordering.api.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CakeFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "FCMService"
        
        // Notification Channels
        const val CHANNEL_ORDERS = "orders_channel"
        const val CHANNEL_MESSAGES = "messages_channel"
        const val CHANNEL_PROMOTIONS = "promotions_channel"
        const val CHANNEL_DELIVERY = "delivery_channel"
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        
        Log.d(TAG, "Message received from: ${remoteMessage.from}")
        
        // Check if message contains data payload
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Message data: ${remoteMessage.data}")
            handleDataMessage(remoteMessage.data)
        }
        
        // Check if message contains notification payload
        remoteMessage.notification?.let { notification ->
            Log.d(TAG, "Notification: ${notification.title} - ${notification.body}")
            showNotification(
                title = notification.title ?: "CakeConnect",
                body = notification.body ?: "",
                data = remoteMessage.data
            )
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM token: $token")
        
        // Save token locally
        val prefs = getSharedPreferences("CakeOrderingPrefs", Context.MODE_PRIVATE)
        prefs.edit().putString("fcm_token", token).apply()
        
        // Send token to server if user is logged in
        sendTokenToServer(token)
    }

    private fun handleDataMessage(data: Map<String, String>) {
        val notificationType = data["type"] ?: "general"
        val title = data["title"] ?: "CakeConnect"
        val body = data["body"] ?: ""
        val orderId = data["order_id"]
        
        showNotification(title, body, data)
        
        // Broadcast to refresh notifications if app is in foreground
        val intent = Intent("com.simats.cakeordering.NOTIFICATION_RECEIVED")
        intent.putExtra("type", notificationType)
        orderId?.let { intent.putExtra("order_id", it.toIntOrNull()) }
        sendBroadcast(intent)
    }

    private fun showNotification(title: String, body: String, data: Map<String, String>) {
        val notificationType = data["type"] ?: "general"
        val orderId = data["order_id"]?.toIntOrNull()
        
        // Determine which activity to open based on notification type
        val intent = when (notificationType) {
            "order_status", "new_order" -> {
                val prefs = getSharedPreferences("CakeOrderingPrefs", Context.MODE_PRIVATE)
                val userType = prefs.getString("user_type", "customer")
                
                when (userType) {
                    "baker" -> Intent(this, BakerOrdersActivity::class.java).apply {
                        putExtra("baker_id", prefs.getInt("baker_id", -1))
                    }
                    "delivery" -> Intent(this, DeliveryDashboardActivity::class.java).apply {
                        putExtra("delivery_id", prefs.getInt("delivery_id", -1))
                    }
                    else -> Intent(this, OrderTrackingActivity::class.java).apply {
                        orderId?.let { putExtra("order_id", it) }
                    }
                }
            }
            "message" -> {
                val prefs = getSharedPreferences("CakeOrderingPrefs", Context.MODE_PRIVATE)
                val userType = prefs.getString("user_type", "customer")
                
                if (userType == "baker") {
                    Intent(this, BakerMessagesActivity::class.java)
                } else {
                    Intent(this, CustomerMessagesActivity::class.java)
                }
            }
            "delivery_assigned", "delivery_update" -> {
                Intent(this, OrderTrackingActivity::class.java).apply {
                    orderId?.let { putExtra("order_id", it) }
                }
            }
            else -> Intent(this, LoginTypeActivity::class.java)
        }
        
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        
        val pendingIntent = PendingIntent.getActivity(
            this,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Select channel based on type
        val channelId = when (notificationType) {
            "order_status", "new_order" -> CHANNEL_ORDERS
            "message" -> CHANNEL_MESSAGES
            "delivery_assigned", "delivery_update" -> CHANNEL_DELIVERY
            "promotion" -> CHANNEL_PROMOTIONS
            else -> CHANNEL_ORDERS
        }

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create notification channel for Android O+
        createNotificationChannel(notificationManager, channelId)

        // Use unique notification ID
        val notificationId = System.currentTimeMillis().toInt()
        notificationManager.notify(notificationId, notificationBuilder.build())
    }

    private fun createNotificationChannel(manager: NotificationManager, channelId: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val (name, description) = when (channelId) {
                CHANNEL_ORDERS -> "Orders" to "Order updates and new orders"
                CHANNEL_MESSAGES -> "Messages" to "Chat messages from bakers/customers"
                CHANNEL_DELIVERY -> "Delivery" to "Delivery status updates"
                CHANNEL_PROMOTIONS -> "Promotions" to "Offers and promotions"
                else -> "General" to "General notifications"
            }
            
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, name, importance).apply {
                this.description = description
                enableVibration(true)
                enableLights(true)
            }
            manager.createNotificationChannel(channel)
        }
    }

    private fun sendTokenToServer(token: String) {
        val prefs = getSharedPreferences("CakeOrderingPrefs", Context.MODE_PRIVATE)
        val userType = prefs.getString("user_type", null) ?: return
        
        val userId = when (userType) {
            "customer" -> prefs.getInt("user_id", -1)
            "baker" -> prefs.getInt("baker_id", -1)
            "delivery" -> prefs.getInt("delivery_id", -1)
            else -> -1
        }
        
        if (userId == -1) return
        
        val body = mapOf(
            "user_type" to userType,
            "user_id" to userId,
            "fcm_token" to token
        )
        
        ApiClient.api.saveFcmToken(body).enqueue(object : Callback<BasicResponse> {
            override fun onResponse(call: Call<BasicResponse>, response: Response<BasicResponse>) {
                if (response.isSuccessful) {
                    Log.d(TAG, "FCM token saved to server")
                } else {
                    Log.e(TAG, "Failed to save FCM token: ${response.code()}")
                }
            }
            
            override fun onFailure(call: Call<BasicResponse>, t: Throwable) {
                Log.e(TAG, "Error saving FCM token: ${t.message}")
            }
        })
    }
}
