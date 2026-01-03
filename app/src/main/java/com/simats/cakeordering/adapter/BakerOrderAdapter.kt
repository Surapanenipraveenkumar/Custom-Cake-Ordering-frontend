package com.simats.cakeordering.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.simats.cakeordering.R
import com.simats.cakeordering.model.BakerOrder

class BakerOrderAdapter(
    private var orders: MutableList<BakerOrder>,
    private val onActionClick: (BakerOrder) -> Unit
) : RecyclerView.Adapter<BakerOrderAdapter.OrderViewHolder>() {

    companion object {
        private const val BASE_URL = "https://zgt68nw9-80.inc1.devtunnels.ms/Custom-Cake-Ordering/"
    }

    inner class OrderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgCake: ImageView = view.findViewById(R.id.imgCake)
        val txtCakeName: TextView = view.findViewById(R.id.txtCakeName)
        val txtStatus: TextView = view.findViewById(R.id.txtStatus)
        val txtCustomerName: TextView = view.findViewById(R.id.txtCustomerName)
        val txtPrice: TextView = view.findViewById(R.id.txtPrice)
        val txtDateTime: TextView = view.findViewById(R.id.txtDateTime)
        val txtDeliveryType: TextView = view.findViewById(R.id.txtDeliveryType)
        val txtCustomOptions: TextView = view.findViewById(R.id.txtCustomOptions)
        val txtDeliveryAddress: TextView = view.findViewById(R.id.txtDeliveryAddress)
        val btnAction: Button = view.findViewById(R.id.btnAction)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_baker_order, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orders[position]

        holder.txtCakeName.text = order.cake_name
        holder.txtCustomerName.text = "for ${order.customer_name}"
        holder.txtPrice.text = "₹${order.price.toInt()}"
        holder.txtDateTime.text = "📅 ${order.order_date}"
        
        // Delivery type
        val deliveryIcon = if (order.delivery_type == "delivery") "🚗" else "📦"
        val deliveryText = if (order.delivery_type == "delivery") "Delivery" else "Pickup"
        holder.txtDeliveryType.text = "$deliveryIcon $deliveryText"

        // Delivery Address (show for delivery orders)
        if (order.delivery_type == "delivery" && !order.delivery_address.isNullOrEmpty()) {
            holder.txtDeliveryAddress.text = "📍 ${order.delivery_address}"
            holder.txtDeliveryAddress.visibility = View.VISIBLE
        } else {
            holder.txtDeliveryAddress.visibility = View.GONE
        }

        // Custom options
        if (order.custom_options != null && order.custom_options.isNotEmpty()) {
            holder.txtCustomOptions.text = "Custom: ${order.custom_options}"
            holder.txtCustomOptions.visibility = View.VISIBLE
        } else {
            holder.txtCustomOptions.visibility = View.GONE
        }

        // Load cake image from database
        if (!order.cake_image.isNullOrEmpty()) {
            val imageUrl = when {
                order.cake_image.startsWith("http") -> order.cake_image
                order.cake_image.startsWith("/") -> BASE_URL + order.cake_image.substring(1)
                else -> BASE_URL + order.cake_image
            }
            
            Glide.with(holder.itemView.context)
                .load(imageUrl)
                .placeholder(R.drawable.ic_placeholder_image)
                .error(R.drawable.ic_placeholder_image)
                .centerCrop()
                .into(holder.imgCake)
        } else {
            holder.imgCake.setImageResource(R.drawable.ic_placeholder_image)
        }

        // Status styling
        when (order.status.lowercase()) {
            "pending" -> {
                holder.txtStatus.text = "⏱ pending"
                holder.txtStatus.setTextColor(0xFFF59E0B.toInt())
                holder.txtStatus.setBackgroundResource(R.drawable.bg_status_pending)
                holder.btnAction.text = "Start Baking"
                holder.btnAction.setBackgroundColor(0xFF3B82F6.toInt())
                holder.btnAction.visibility = View.VISIBLE
            }
            "in_progress", "in progress" -> {
                holder.txtStatus.text = "⏳ in progress"
                holder.txtStatus.setTextColor(0xFF3B82F6.toInt())
                holder.txtStatus.setBackgroundResource(R.drawable.bg_status_progress)
                holder.btnAction.text = "Mark as Ready"
                holder.btnAction.setBackgroundColor(0xFF10B981.toInt())
                holder.btnAction.visibility = View.VISIBLE
            }
            "ready" -> {
                holder.txtStatus.text = "✓ ready"
                holder.txtStatus.setTextColor(0xFF10B981.toInt())
                holder.txtStatus.setBackgroundResource(R.drawable.bg_status_ready)
                holder.btnAction.text = "Ready for Delivery"
                holder.btnAction.setBackgroundColor(0xFF8B5CF6.toInt())
                holder.btnAction.visibility = View.VISIBLE
            }
            "delivered" -> {
                holder.txtStatus.text = "✓ delivered"
                holder.txtStatus.setTextColor(0xFF6B7280.toInt())
                holder.txtStatus.setBackgroundResource(R.drawable.bg_status_ready)
                holder.btnAction.visibility = View.GONE
            }
            else -> {
                holder.txtStatus.text = order.status
                holder.btnAction.visibility = View.GONE
            }
        }

        holder.btnAction.setOnClickListener {
            onActionClick(order)
        }
    }

    override fun getItemCount() = orders.size

    fun updateData(newOrders: List<BakerOrder>) {
        orders.clear()
        orders.addAll(newOrders)
        notifyDataSetChanged()
    }
}
