package com.simats.cakeordering.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.simats.cakeordering.R
import com.simats.cakeordering.databinding.ItemCartBinding
import com.simats.cakeordering.model.CartItem

class CartAdapter(
    private var items: MutableList<CartItem>,
    private val baseUrl: String,
    private val onQuantityChange: (CartItem, Int) -> Unit,
    private val onDelete: (CartItem) -> Unit
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    companion object {
        private const val TAG = "CartAdapter"
    }

    inner class CartViewHolder(val binding: ItemCartBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        Log.d(TAG, "onCreateViewHolder called")
        val binding = ItemCartBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        Log.d(TAG, "ViewHolder created successfully")
        return CartViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val item = items[position]
        val context = holder.itemView.context

        Log.d(TAG, "Binding item $position: ${item.cakeName}")

        with(holder.binding) {
            // Cake name
            tvCakeName.text = item.cakeName

            // Baker name (using shop name or default)
            tvBakerName.text = "by Cake Shop"

            // Weight/Portion
            if (!item.weight.isNullOrEmpty()) {
                tvPortion.text = item.weight
                tvPortion.visibility = View.VISIBLE
            } else {
                tvPortion.visibility = View.GONE
            }

            // Price
            tvPrice.text = "₹${item.itemTotal.toInt()}"

            // Quantity
            tvQuantity.text = item.quantity.toString()

            // Customization tags
            val tags = mutableListOf<String>()
            item.shape?.let { if (it.isNotEmpty()) tags.add(it) }
            item.flavor?.let { if (it.isNotEmpty()) tags.add(it) }
            item.color?.let { if (it.isNotEmpty()) tags.add(it) }

            // Show up to 3 tags
            if (tags.size > 0) {
                tvTag1.text = tags[0]
                tvTag1.visibility = View.VISIBLE
            } else {
                tvTag1.visibility = View.GONE
            }
            
            if (tags.size > 1) {
                tvTag2.text = tags[1]
                tvTag2.visibility = View.VISIBLE
            } else {
                tvTag2.visibility = View.GONE
            }
            
            if (tags.size > 2) {
                tvTag3.text = tags[2]
                tvTag3.visibility = View.VISIBLE
            } else {
                tvTag3.visibility = View.GONE
            }

            // Load image
            val imageUrl = baseUrl + item.image
            Glide.with(context)
                .load(imageUrl)
                .centerCrop()
                .placeholder(R.drawable.ic_placeholder_image)
                .error(R.drawable.ic_placeholder_image)
                .into(ivCakeImage)

            // Quantity controls
            btnMinus.setOnClickListener {
                Log.d(TAG, "Minus clicked for ${item.cakeName}, current qty: ${item.quantity}")
                if (item.quantity > 1) {
                    onQuantityChange(item, item.quantity - 1)
                } else {
                    Log.d(TAG, "Cannot decrease - quantity is already 1")
                }
            }

            btnPlus.setOnClickListener {
                Log.d(TAG, "Plus clicked for ${item.cakeName}, current qty: ${item.quantity}")
                onQuantityChange(item, item.quantity + 1)
            }

            // Delete button
            btnDelete.setOnClickListener {
                Log.d(TAG, "Delete clicked for ${item.cakeName}")
                onDelete(item)
            }
        }
    }

    override fun getItemCount(): Int {
        Log.d(TAG, "getItemCount: ${items.size}")
        return items.size
    }

    fun updateItems(newItems: List<CartItem>) {
        Log.d(TAG, "updateItems called with ${newItems.size} items")
        
        // IMPORTANT: Make a copy FIRST because items and newItems may be the same reference!
        val itemsCopy = newItems.toList()
        Log.d(TAG, "Copied ${itemsCopy.size} items")
        
        items.clear()
        items.addAll(itemsCopy)
        Log.d(TAG, "Items after update: ${items.size}")
        
        // Log each item for debugging
        items.forEachIndexed { index, item ->
            Log.d(TAG, "  Item[$index]: ${item.cakeName}, price=${item.price}")
        }
        
        notifyDataSetChanged()
        Log.d(TAG, "notifyDataSetChanged called")
    }

    fun removeItem(cartId: Int) {
        val index = items.indexOfFirst { it.cartId == cartId }
        if (index != -1) {
            items.removeAt(index)
            notifyItemRemoved(index)
        }
    }
}
