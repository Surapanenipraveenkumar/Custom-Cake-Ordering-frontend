package com.simats.cakeordering.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.simats.cakeordering.R
import com.simats.cakeordering.model.CustomerOrder

class CustomerOrdersAdapter(
    private var orders: List<CustomerOrder>,
    private val onOrderClick: (CustomerOrder) -> Unit
) : RecyclerView.Adapter<CustomerOrdersAdapter.OrderViewHolder>() {

    inner class OrderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgOrderIcon: ImageView = view.findViewById(R.id.imgOrderIcon)
        val tvOrderId: TextView = view.findViewById(R.id.tvOrderId)
        val tvBakerName: TextView = view.findViewById(R.id.tvBakerName)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
        val imgCake: ImageView = view.findViewById(R.id.imgCake)
        val tvCakeName: TextView = view.findViewById(R.id.tvCakeName)
        val tvOrderDate: TextView = view.findViewById(R.id.tvOrderDate)
        val tvPrice: TextView = view.findViewById(R.id.tvPrice)
        val btnView: ImageView = view.findViewById(R.id.btnView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_customer_order, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orders[position]
        val context = holder.itemView.context

        holder.tvOrderId.text = "Order ${order.orderIdStr}"
        holder.tvBakerName.text = order.bakerName
        holder.tvCakeName.text = order.cakeName
        holder.tvOrderDate.text = order.orderDate
        holder.tvPrice.text = "₹${order.totalAmount.toInt()}"

        // Load cake image
        if (!order.cakeImage.isNullOrEmpty()) {
            val imageUrl = if (order.cakeImage.startsWith("http")) {
                order.cakeImage
            } else {
                "https://zgt68nw9-80.inc1.devtunnels.ms/Custom-Cake-Ordering/${order.cakeImage}"
            }
            
            Glide.with(context)
                .load(imageUrl)
                .placeholder(R.drawable.sample_cake)
                .error(R.drawable.sample_cake)
                .into(holder.imgCake)
        }

        // Status styling
        when (order.status.lowercase()) {
            "pending" -> {
                holder.tvStatus.text = "Pending"
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_pending)
                holder.tvStatus.setTextColor(0xFFF59E0B.toInt())
                holder.imgOrderIcon.setImageResource(R.drawable.ic_pending)
                holder.imgOrderIcon.setColorFilter(0xFFF59E0B.toInt())
            }
            "in_progress", "in progress" -> {
                holder.tvStatus.text = "In Progress"
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_progress)
                holder.tvStatus.setTextColor(0xFF3B82F6.toInt())
                holder.imgOrderIcon.setImageResource(R.drawable.ic_pending)
                holder.imgOrderIcon.setColorFilter(0xFF3B82F6.toInt())
            }
            "ready", "delivered" -> {
                holder.tvStatus.text = "Delivered"
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_delivered)
                holder.tvStatus.setTextColor(0xFFFFFFFF.toInt())
                holder.imgOrderIcon.setImageResource(R.drawable.ic_check_circle)
                holder.imgOrderIcon.setColorFilter(0xFF10B981.toInt())
            }
            "cancelled" -> {
                holder.tvStatus.text = "Cancelled"
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_cancelled)
                holder.tvStatus.setTextColor(0xFFEF4444.toInt())
                holder.imgOrderIcon.setImageResource(R.drawable.ic_cancel)
                holder.imgOrderIcon.setColorFilter(0xFFEF4444.toInt())
            }
            else -> {
                holder.tvStatus.text = order.status
                holder.imgOrderIcon.setImageResource(R.drawable.ic_pending)
            }
        }

        holder.itemView.setOnClickListener { onOrderClick(order) }
        holder.btnView.setOnClickListener { onOrderClick(order) }
    }

    override fun getItemCount() = orders.size

    fun updateData(newOrders: List<CustomerOrder>) {
        orders = newOrders
        notifyDataSetChanged()
    }
}
