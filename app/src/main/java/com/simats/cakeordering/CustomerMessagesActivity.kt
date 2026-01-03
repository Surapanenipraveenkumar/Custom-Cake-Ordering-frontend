package com.simats.cakeordering

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.simats.cakeordering.adapter.CustomerBakerChatAdapter
import com.simats.cakeordering.api.ApiClient
import com.simats.cakeordering.model.CustomerMessagesResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CustomerMessagesActivity : AppCompatActivity() {

    private var userId: Int = 0
    private lateinit var rvBakers: RecyclerView
    private lateinit var emptyState: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer_messages)

        userId = getSharedPreferences("CakeOrderingPrefs", MODE_PRIVATE)
            .getInt("user_id", 0)

        if (userId == 0) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupViews()
        loadBakerConversations()
    }

    private fun setupViews() {
        rvBakers = findViewById(R.id.rvBakers)
        emptyState = findViewById(R.id.emptyState)

        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        rvBakers.layoutManager = LinearLayoutManager(this)
    }

    private fun loadBakerConversations() {
        ApiClient.api.getCustomerMessages(userId)
            .enqueue(object : Callback<CustomerMessagesResponse> {
                override fun onResponse(
                    call: Call<CustomerMessagesResponse>,
                    response: Response<CustomerMessagesResponse>
                ) {
                    if (response.isSuccessful && response.body()?.status == "success") {
                        val bakers = response.body()?.bakers ?: emptyList()
                        if (bakers.isEmpty()) {
                            showEmpty()
                        } else {
                            showBakers(bakers)
                        }
                    } else {
                        showEmpty()
                    }
                }

                override fun onFailure(call: Call<CustomerMessagesResponse>, t: Throwable) {
                    Toast.makeText(
                        this@CustomerMessagesActivity,
                        "Connection error: ${t.message}",
                        Toast.LENGTH_LONG
                    ).show()
                    showEmpty()
                }
            })
    }

    private fun showEmpty() {
        rvBakers.visibility = View.GONE
        emptyState.visibility = View.VISIBLE
    }

    private fun showBakers(bakers: List<com.simats.cakeordering.model.BakerChat>) {
        rvBakers.visibility = View.VISIBLE
        emptyState.visibility = View.GONE

        // Simple adapter for baker list
        rvBakers.adapter = CustomerBakerChatAdapter(bakers) { baker ->
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("baker_id", baker.bakerId)
            intent.putExtra("baker_name", baker.shopName)
            intent.putExtra("is_customer", true)
            startActivity(intent)
        }
    }
}
