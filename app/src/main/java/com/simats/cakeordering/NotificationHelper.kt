package com.simats.cakeordering

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging

/**
 * Helper class for managing notifications and FCM tokens
 */
object NotificationHelper {

    private const val TAG = "NotificationHelper"

    /**
     * Creates all notification channels on app startup (Android O+)
     */
    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Orders Channel
            val ordersChannel = NotificationChannel(
                CakeFirebaseMessagingService.CHANNEL_ORDERS,
                "Orders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Order updates and new orders"
                enableVibration(true)
                enableLights(true)
            }

            // Messages Channel
            val messagesChannel = NotificationChannel(
                CakeFirebaseMessagingService.CHANNEL_MESSAGES,
                "Messages",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Chat messages"
                enableVibration(true)
            }

            // Delivery Channel
            val deliveryChannel = NotificationChannel(
                CakeFirebaseMessagingService.CHANNEL_DELIVERY,
                "Delivery",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Delivery updates"
                enableVibration(true)
            }

            // Promotions Channel
            val promotionsChannel = NotificationChannel(
                CakeFirebaseMessagingService.CHANNEL_PROMOTIONS,
                "Promotions",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Offers and promotions"
            }

            notificationManager.createNotificationChannels(
                listOf(ordersChannel, messagesChannel, deliveryChannel, promotionsChannel)
            )
            
            Log.d(TAG, "Notification channels created")
        }
    }

    /**
     * Gets the current FCM token
     */
    fun getFcmToken(callback: (String?) -> Unit) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                Log.d(TAG, "FCM Token: $token")
                callback(token)
            } else {
                Log.e(TAG, "Failed to get FCM token", task.exception)
                callback(null)
            }
        }
    }

    /**
     * Saves FCM token to SharedPreferences and server
     */
    fun saveTokenToServerAfterLogin(context: Context, userType: String, userId: Int) {
        getFcmToken { token ->
            if (token != null) {
                // Save locally
                val prefs = context.getSharedPreferences("CakeOrderingPrefs", Context.MODE_PRIVATE)
                prefs.edit()
                    .putString("fcm_token", token)
                    .putString("user_type", userType)
                    .apply()

                // Save to server
                val body = mapOf(
                    "user_type" to userType,
                    "user_id" to userId,
                    "fcm_token" to token
                )

                com.simats.cakeordering.api.ApiClient.api.saveFcmToken(body)
                    .enqueue(object : retrofit2.Callback<BasicResponse> {
                        override fun onResponse(
                            call: retrofit2.Call<BasicResponse>,
                            response: retrofit2.Response<BasicResponse>
                        ) {
                            if (response.isSuccessful) {
                                Log.d(TAG, "FCM token saved to server for $userType #$userId")
                            } else {
                                Log.e(TAG, "Failed to save token: ${response.code()}")
                            }
                        }

                        override fun onFailure(call: retrofit2.Call<BasicResponse>, t: Throwable) {
                            Log.e(TAG, "Error saving token: ${t.message}")
                        }
                    })
            }
        }
    }

    /**
     * Clears FCM token on logout
     */
    fun clearTokenOnLogout(context: Context) {
        val prefs = context.getSharedPreferences("CakeOrderingPrefs", Context.MODE_PRIVATE)
        prefs.edit()
            .remove("fcm_token")
            .remove("user_type")
            .apply()
        
        Log.d(TAG, "FCM token cleared locally")
    }
}
