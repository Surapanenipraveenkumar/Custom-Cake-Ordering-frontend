package com.simats.cakeordering

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.simats.cakeordering.adapter.MessageCustomerAdapter
import com.simats.cakeordering.api.ApiClient
import com.simats.cakeordering.model.CustomerMessagesResponse
import com.simats.cakeordering.model.MessageCustomer
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BakerMessagesActivity : AppCompatActivity() {

    private lateinit var rvMessages: RecyclerView
    private lateinit var txtEmpty: TextView
    private lateinit var customerAdapter: MessageCustomerAdapter

    private var bakerId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_baker_messages)

        bakerId = intent.getIntExtra("baker_id", 0)

        // Initialize views
        rvMessages = findViewById(R.id.rvMessages)
        txtEmpty = findViewById(R.id.txtEmpty)

        // Back button
        findViewById<TextView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        // Setup RecyclerView
        customerAdapter = MessageCustomerAdapter(mutableListOf()) { customer ->
            openChat(customer)
        }
        rvMessages.layoutManager = LinearLayoutManager(this)
        rvMessages.adapter = customerAdapter

        // Load customers
        loadCustomers()
    }

    private fun loadCustomers() {
        android.util.Log.d("BakerMessages", "Loading customers for bakerId: $bakerId")
        
        ApiClient.api.getBakerMessageCustomers(bakerId)
            .enqueue(object : Callback<CustomerMessagesResponse> {
                override fun onResponse(
                    call: Call<CustomerMessagesResponse>,
                    response: Response<CustomerMessagesResponse>
                ) {
                    android.util.Log.d("BakerMessages", "Response code: ${response.code()}")
                    android.util.Log.d("BakerMessages", "Response body: ${response.body()}")
                    
                    if (response.isSuccessful && response.body()?.status == "success") {
                        val customers = response.body()?.customers ?: emptyList()
                        android.util.Log.d("BakerMessages", "Customers count: ${customers.size}")
                        customerAdapter.updateData(customers)

                        if (customers.isEmpty()) {
                            txtEmpty.visibility = View.VISIBLE
                            txtEmpty.text = "No messages yet"
                            rvMessages.visibility = View.GONE
                        } else {
                            txtEmpty.visibility = View.GONE
                            rvMessages.visibility = View.VISIBLE
                        }
                    } else {
                        val errorBody = response.errorBody()?.string()
                        android.util.Log.e("BakerMessages", "API Error: ${response.body()?.status}, errorBody: $errorBody")
                        txtEmpty.visibility = View.VISIBLE
                        txtEmpty.text = "Error loading messages"
                        rvMessages.visibility = View.GONE
                    }
                }

                override fun onFailure(call: Call<CustomerMessagesResponse>, t: Throwable) {
                    android.util.Log.e("BakerMessages", "Network Error", t)
                    Toast.makeText(
                        this@BakerMessagesActivity,
                        "Error: ${t.message}",
                        Toast.LENGTH_LONG
                    ).show()
                    txtEmpty.visibility = View.VISIBLE
                    txtEmpty.text = "Connection error"
                    rvMessages.visibility = View.GONE
                }
            })
    }

    private fun openChat(customer: MessageCustomer) {
        val intent = Intent(this, ChatActivity::class.java)
        intent.putExtra("baker_id", bakerId)
        intent.putExtra("user_id", customer.user_id)
        intent.putExtra("customer_name", customer.name)
        startActivity(intent)
    }
}
