package com.simats.cakeordering

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.simats.cakeordering.adapter.NotificationAdapter
import com.simats.cakeordering.api.ApiClient
import com.simats.cakeordering.databinding.ActivityNotificationsBinding
import com.simats.cakeordering.model.BasicResponse
import com.simats.cakeordering.model.NotificationItem
import com.simats.cakeordering.model.NotificationsResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BakerNotificationsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNotificationsBinding
    private lateinit var adapter: NotificationAdapter
    private var bakerId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bakerId = intent.getIntExtra("baker_id", -1)
        
        if (bakerId == -1) {
            // Try getting from SharedPreferences as fallback
            val prefs = getSharedPreferences("CakeOrderingPrefs", MODE_PRIVATE)
            bakerId = prefs.getInt("baker_id", -1)
        }

        if (bakerId == -1) {
            Toast.makeText(this, "Please login again", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupRecyclerView()
        setupClickListeners()
        loadNotifications()
    }

    private fun setupRecyclerView() {
        adapter = NotificationAdapter { notification ->
            markAsRead(notification)
            
            // Navigate to order details if applicable
            if (notification.orderId != null) {
                val intent = Intent(this, BakerOrdersActivity::class.java)
                intent.putExtra("baker_id", bakerId)
                startActivity(intent)
            }
        }
        
        binding.rvNotifications.layoutManager = LinearLayoutManager(this)
        binding.rvNotifications.adapter = adapter
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener { finish() }
        
        binding.tvMarkAllRead.setOnClickListener {
            markAllAsRead()
        }
    }

    private fun loadNotifications() {
        binding.progressBar.visibility = View.VISIBLE
        binding.emptyState.visibility = View.GONE
        binding.rvNotifications.visibility = View.GONE

        ApiClient.api.getNotifications("baker", bakerId)
            .enqueue(object : Callback<NotificationsResponse> {
                override fun onResponse(
                    call: Call<NotificationsResponse>,
                    response: Response<NotificationsResponse>
                ) {
                    binding.progressBar.visibility = View.GONE
                    
                    val body = response.body()
                    if (response.isSuccessful && body?.status == "success") {
                        if (body.notifications.isEmpty()) {
                            binding.emptyState.visibility = View.VISIBLE
                            binding.rvNotifications.visibility = View.GONE
                        } else {
                            binding.rvNotifications.visibility = View.VISIBLE
                            adapter.submitList(body.notifications)
                            
                            if (body.unreadCount > 0) {
                                binding.unreadBanner.visibility = View.VISIBLE
                                binding.tvUnreadCount.text = "${body.unreadCount} unread notifications"
                            } else {
                                binding.unreadBanner.visibility = View.GONE
                            }
                        }
                    } else {
                        binding.emptyState.visibility = View.VISIBLE
                    }
                }

                override fun onFailure(call: Call<NotificationsResponse>, t: Throwable) {
                    binding.progressBar.visibility = View.GONE
                    binding.emptyState.visibility = View.VISIBLE
                    Toast.makeText(this@BakerNotificationsActivity, 
                        "Failed to load notifications", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun markAsRead(notification: NotificationItem) {
        if (notification.isRead) return
        
        val body = mapOf(
            "notification_id" to notification.notificationId
        )
        
        ApiClient.api.markNotificationRead(body)
            .enqueue(object : Callback<BasicResponse> {
                override fun onResponse(call: Call<BasicResponse>, response: Response<BasicResponse>) {}
                override fun onFailure(call: Call<BasicResponse>, t: Throwable) {}
            })
    }

    private fun markAllAsRead() {
        val body = mapOf(
            "mark_all" to true,
            "user_id" to bakerId,
            "user_type" to "baker"
        )
        
        ApiClient.api.markNotificationRead(body)
            .enqueue(object : Callback<BasicResponse> {
                override fun onResponse(call: Call<BasicResponse>, response: Response<BasicResponse>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@BakerNotificationsActivity, 
                            "All marked as read", Toast.LENGTH_SHORT).show()
                        loadNotifications()
                    }
                }
                override fun onFailure(call: Call<BasicResponse>, t: Throwable) {}
            })
    }
}
