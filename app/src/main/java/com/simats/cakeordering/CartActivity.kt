package com.simats.cakeordering

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.simats.cakeordering.adapter.CartAdapter
import com.simats.cakeordering.api.ApiClient
import com.simats.cakeordering.databinding.ActivityCartBinding
import com.simats.cakeordering.model.BasicResponse
import com.simats.cakeordering.model.CartItem
import com.simats.cakeordering.model.CartResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CartActivity : AppCompatActivity() {

    companion object {
        private const val BASE_URL = "https://zgt68nw9-80.inc1.devtunnels.ms/Custom-Cake-Ordering/"
        private const val TAG = "CartActivity"
        private const val DELIVERY_FEE = 50
    }

    private lateinit var binding: ActivityCartBinding
    private lateinit var adapter: CartAdapter
    private var cartItems = mutableListOf<CartItem>()
    private var userId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get user ID
        val prefs = getSharedPreferences("CakeOrderingPrefs", Context.MODE_PRIVATE)
        userId = prefs.getInt("user_id", 0)
        
        Log.d(TAG, "onCreate - userId: $userId")

        setupRecyclerView()
        setupClickListeners()
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume - loading cart")
        loadCart()
    }

    private fun setupRecyclerView() {
        adapter = CartAdapter(
            items = cartItems,
            baseUrl = BASE_URL,
            onQuantityChange = { item, newQuantity ->
                updateCartQuantity(item, newQuantity)
            },
            onDelete = { item ->
                showDeleteConfirmation(item)
            }
        )
        binding.recyclerCart.layoutManager = LinearLayoutManager(this)
        binding.recyclerCart.adapter = adapter
        
        Log.d(TAG, "RecyclerView setup complete")
    }

    private fun setupClickListeners() {
        // Back button
        binding.btnBack.setOnClickListener {
            finish()
        }

        // Browse cakes (when cart is empty)
        binding.btnBrowseCakes.setOnClickListener {
            finish()
        }

        // Checkout
        binding.btnCheckout.setOnClickListener {
            if (cartItems.isEmpty()) {
                Toast.makeText(this, "Your cart is empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            // Calculate subtotal
            val subtotal = cartItems.sumOf { it.itemTotal }
            
            // Prepare item names and prices
            val itemNames = ArrayList(cartItems.map { it.cakeName })
            val itemPrices = ArrayList(cartItems.map { it.itemTotal.toInt() })
            
            // Navigate to checkout
            val intent = Intent(this, CheckoutActivity::class.java).apply {
                putExtra("subtotal", subtotal)
                putStringArrayListExtra("items", itemNames)
                putIntegerArrayListExtra("prices", itemPrices)
            }
            startActivity(intent)
        }
    }

    private fun loadCart() {
        if (userId == 0) {
            Log.e(TAG, "No user ID - showing empty cart")
            showEmptyCart()
            return
        }

        Log.d(TAG, "Loading cart for user: $userId")
        binding.progressBar.visibility = View.VISIBLE
        binding.recyclerCart.visibility = View.GONE

        // Try GET first, then POST as fallback
        ApiClient.api.getCart(userId)
            .enqueue(object : Callback<CartResponse> {
                override fun onResponse(
                    call: Call<CartResponse>,
                    response: Response<CartResponse>
                ) {
                    binding.progressBar.visibility = View.GONE
                    Log.d(TAG, "Cart API response code: ${response.code()}")
                    Log.d(TAG, "Cart API response body: ${response.body()}")

                    if (response.isSuccessful && response.body()?.status == "success") {
                        val cart = response.body()!!
                        Log.d(TAG, "Cart loaded successfully: ${cart.cart.size} items")
                        
                        // Log each item
                        cart.cart.forEachIndexed { index, item ->
                            Log.d(TAG, "Item[$index]: ${item.cakeName}, qty=${item.quantity}, price=${item.price}")
                        }

                        if (cart.cart.isEmpty()) {
                            showEmptyCart()
                        } else {
                            cartItems.clear()
                            cartItems.addAll(cart.cart)
                            
                            // Re-set adapter to force refresh
                            binding.recyclerCart.adapter = adapter
                            adapter.updateItems(cartItems)
                            updatePrices(cart.cartTotal)
                            showCartItems()
                            
                            Log.d(TAG, "Cart displayed with ${cartItems.size} items")
                        }
                    } else {
                        Log.e(TAG, "Cart response status not success: ${response.body()?.status}")
                        // Try POST method as fallback
                        loadCartWithPost()
                    }
                }

                override fun onFailure(call: Call<CartResponse>, t: Throwable) {
                    Log.e(TAG, "GET failed, trying POST: ${t.message}")
                    // Try POST method as fallback
                    loadCartWithPost()
                }
            })
    }

    private fun loadCartWithPost() {
        Log.d(TAG, "Trying POST method for cart")
        
        ApiClient.api.getCartPost(userId)
            .enqueue(object : Callback<CartResponse> {
                override fun onResponse(
                    call: Call<CartResponse>,
                    response: Response<CartResponse>
                ) {
                    binding.progressBar.visibility = View.GONE
                    
                    if (response.isSuccessful && response.body()?.status == "success") {
                        val cart = response.body()!!
                        Log.d(TAG, "POST Cart loaded: ${cart.cart.size} items")

                        if (cart.cart.isEmpty()) {
                            showEmptyCart()
                        } else {
                            cartItems.clear()
                            cartItems.addAll(cart.cart)
                            
                            // Re-set adapter to force refresh
                            binding.recyclerCart.adapter = adapter
                            adapter.updateItems(cartItems)
                            updatePrices(cart.cartTotal)
                            showCartItems()
                        }
                    } else {
                        Log.e(TAG, "POST also failed")
                        showEmptyCart()
                    }
                }

                override fun onFailure(call: Call<CartResponse>, t: Throwable) {
                    binding.progressBar.visibility = View.GONE
                    Log.e(TAG, "Both GET and POST failed", t)
                    showEmptyCart()
                    Toast.makeText(this@CartActivity, "Cannot connect to server", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun showCartItems() {
        Log.d(TAG, "showCartItems() - ${cartItems.size} items in list")
        Log.d(TAG, "Adapter item count: ${adapter.itemCount}")
        
        binding.progressBar.visibility = View.GONE
        binding.emptyCartLayout.visibility = View.GONE
        binding.recyclerCart.visibility = View.VISIBLE
        binding.checkoutSection.visibility = View.VISIBLE
        
        // Force refresh the adapter
        runOnUiThread {
            adapter.notifyDataSetChanged()
            binding.recyclerCart.invalidate()
            binding.recyclerCart.requestLayout()
            Log.d(TAG, "RecyclerView refreshed, adapter count: ${adapter.itemCount}")
        }
    }

    private fun showEmptyCart() {
        Log.d(TAG, "showEmptyCart()")
        binding.recyclerCart.visibility = View.GONE
        binding.checkoutSection.visibility = View.GONE
        binding.emptyCartLayout.visibility = View.VISIBLE
    }

    private fun updatePrices(subtotal: Double) {
        val subtotalInt = subtotal.toInt()
        val total = subtotalInt + DELIVERY_FEE
        
        binding.tvSubtotal.text = "₹$subtotalInt"
        binding.tvDeliveryFee.text = "₹$DELIVERY_FEE"
        binding.tvTotal.text = "₹$total"
    }

    private fun updateCartQuantity(item: CartItem, newQuantity: Int) {
        Log.d(TAG, "Updating quantity for cart ${item.cartId} to $newQuantity")
        
        val body = mapOf(
            "cart_id" to item.cartId,
            "quantity" to newQuantity
        )

        ApiClient.api.updateCart(body)
            .enqueue(object : Callback<BasicResponse> {
                override fun onResponse(
                    call: Call<BasicResponse>,
                    response: Response<BasicResponse>
                ) {
                    if (response.isSuccessful && response.body()?.status == "success") {
                        Log.d(TAG, "Quantity updated successfully")
                        loadCart()
                    } else {
                        Toast.makeText(
                            this@CartActivity,
                            "Failed to update quantity",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<BasicResponse>, t: Throwable) {
                    Toast.makeText(this@CartActivity, "Connection error", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun showDeleteConfirmation(item: CartItem) {
        AlertDialog.Builder(this)
            .setTitle("Remove Item")
            .setMessage("Remove ${item.cakeName} from cart?")
            .setPositiveButton("Remove") { _, _ ->
                deleteCartItem(item)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteCartItem(item: CartItem) {
        Log.d(TAG, "Deleting cart item: ${item.cartId}")
        
        ApiClient.api.deleteCartItem(item.cartId)
            .enqueue(object : Callback<BasicResponse> {
                override fun onResponse(
                    call: Call<BasicResponse>,
                    response: Response<BasicResponse>
                ) {
                    if (response.isSuccessful && response.body()?.status == "success") {
                        Log.d(TAG, "Item deleted successfully")
                        adapter.removeItem(item.cartId)
                        cartItems.removeAll { it.cartId == item.cartId }

                        if (cartItems.isEmpty()) {
                            showEmptyCart()
                        } else {
                            val newSubtotal = cartItems.sumOf { it.itemTotal }
                            updatePrices(newSubtotal)
                        }

                        Toast.makeText(this@CartActivity, "Item removed", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(
                            this@CartActivity,
                            "Failed to remove item",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<BasicResponse>, t: Throwable) {
                    Toast.makeText(this@CartActivity, "Connection error", Toast.LENGTH_SHORT).show()
                }
            })
    }
}
