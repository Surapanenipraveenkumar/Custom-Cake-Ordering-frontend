package com.simats.cakeordering.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.simats.cakeordering.R
import com.simats.cakeordering.model.ProfileOrder

class ProfileOrderAdapter(
    private var orders: List<ProfileOrder>
) : RecyclerView.Adapter<ProfileOrderAdapter.OrderViewHolder>() {

    companion object {
        private const val BASE_URL = "https://zgt68nw9-80.inc1.devtunnels.ms/Custom-Cake-Ordering/"
    }

    inner class OrderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgCake: ImageView = view.findViewById(R.id.imgCake)
        val tvOrderId: TextView = view.findViewById(R.id.tvOrderId)
        val tvCakeName: TextView = view.findViewById(R.id.tvCakeName)
        val tvOrderDate: TextView = view.findViewById(R.id.tvOrderDate)
        val tvOrderTime: TextView = view.findViewById(R.id.tvOrderTime)
        val tvAmount: TextView = view.findViewById(R.id.tvAmount)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_profile_order, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orders[position]

        holder.tvOrderId.text = "#${order.orderIdStr ?: "ORD${order.orderId}"}"
        holder.tvCakeName.text = order.cakeName ?: "Cake Order"
        holder.tvOrderDate.text = "📅 ${order.orderDate ?: ""}"
        holder.tvOrderTime.text = "⏰ ${order.orderTime ?: ""}"
        holder.tvAmount.text = "₹${order.totalAmount.toInt()}"

        // Load cake image from database
        if (!order.cakeImage.isNullOrEmpty()) {
            val imageUrl = when {
                order.cakeImage.startsWith("http") -> order.cakeImage
                order.cakeImage.startsWith("/") -> BASE_URL + order.cakeImage.substring(1)
                else -> BASE_URL + order.cakeImage
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
                holder.tvStatus.text = "PENDING"
                holder.tvStatus.setTextColor(0xFFF59E0B.toInt())
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_pending)
            }
            "in_progress", "in progress" -> {
                holder.tvStatus.text = "IN PROGRESS"
                holder.tvStatus.setTextColor(0xFF3B82F6.toInt())
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_progress)
            }
            "ready" -> {
                holder.tvStatus.text = "READY"
                holder.tvStatus.setTextColor(0xFF10B981.toInt())
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_ready)
            }
            "delivered" -> {
                holder.tvStatus.text = "DELIVERED"
                holder.tvStatus.setTextColor(0xFF10B981.toInt())
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_ready)
            }
            else -> {
                holder.tvStatus.text = order.status.uppercase()
                holder.tvStatus.setTextColor(0xFF6B7280.toInt())
            }
        }
    }

    override fun getItemCount() = orders.size

    fun updateData(newOrders: List<ProfileOrder>) {
        orders = newOrders
        notifyDataSetChanged()
    }
}
