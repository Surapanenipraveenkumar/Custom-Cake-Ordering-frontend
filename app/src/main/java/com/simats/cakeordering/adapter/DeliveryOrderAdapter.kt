package com.simats.cakeordering.adapter

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.simats.cakeordering.R
import com.simats.cakeordering.model.DeliveryOrder

class DeliveryOrderAdapter(
    private val onAcceptClick: (DeliveryOrder) -> Unit,
    private val onViewClick: (DeliveryOrder) -> Unit
) : RecyclerView.Adapter<DeliveryOrderAdapter.ViewHolder>() {

    private val orders = mutableListOf<DeliveryOrder>()

    fun updateData(newOrders: List<DeliveryOrder>) {
        orders.clear()
        orders.addAll(newOrders)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_delivery_order, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(orders[position])
    }

    override fun getItemCount() = orders.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvOrderId: TextView = view.findViewById(R.id.tvOrderId)
        private val tvAmount: TextView = view.findViewById(R.id.tvAmount)
        private val tvCakeName: TextView = view.findViewById(R.id.tvCakeName)
        private val tvBakerName: TextView = view.findViewById(R.id.tvBakerName)
        private val tvDistance: TextView = view.findViewById(R.id.tvDistance)
        private val tvEta: TextView = view.findViewById(R.id.tvEta)
        private val tvPickupName: TextView = view.findViewById(R.id.tvPickupName)
        private val tvPickupAddress: TextView = view.findViewById(R.id.tvPickupAddress)
        private val tvDeliveryName: TextView = view.findViewById(R.id.tvDeliveryName)
        private val tvDeliveryAddress: TextView = view.findViewById(R.id.tvDeliveryAddress)
        private val btnAccept: Button = view.findViewById(R.id.btnAccept)
        private val btnCall: ImageButton = view.findViewById(R.id.btnCall)
        private val tvStatus: TextView = view.findViewById(R.id.tvStatus)
        private val priorityBanner: LinearLayout = view.findViewById(R.id.priorityBanner)

        fun bind(order: DeliveryOrder) {
            // Order ID
            tvOrderId.text = "ORD-${order.orderId}"
            
            // Amount
            val amount = order.totalAmount?.toInt() ?: 0
            tvAmount.text = "₹${String.format("%,d", amount)}"
            
            // Cake name - using baker name as we don't have cake name in model
            tvCakeName.text = "Order #${order.orderId}"
            
            // Baker name
            tvBakerName.text = order.bakerName ?: "Baker"
            
            // Distance - placeholder
            tvDistance.text = "📍 3.2 km"
            
            // ETA - placeholder
            tvEta.text = "🕐 Est. 25 min"
            
            // Pickup info
            tvPickupName.text = order.bakerName ?: "Baker"
            tvPickupAddress.text = order.bakerAddress ?: "Pickup address"
            
            // Delivery info
            tvDeliveryName.text = order.customerName ?: "Customer"
            tvDeliveryAddress.text = order.customerAddress ?: "Delivery address"
            
            // Show priority banner for high value orders
            if ((order.totalAmount ?: 0.0) > 5000) {
                priorityBanner.visibility = View.VISIBLE
            } else {
                priorityBanner.visibility = View.GONE
            }

            // Status handling
            val status = order.deliveryStatus ?: "pending"
            
            if (order.isAssigned == true || status != "pending") {
                // Active order - hide accept, show status
                btnAccept.visibility = View.GONE
                tvStatus.visibility = View.VISIBLE
                tvStatus.text = when (status) {
                    "assigned" -> "In Transit"
                    "picked_up" -> "Picked Up - Delivering"
                    "delivered" -> "Delivered"
                    else -> status.replaceFirstChar { it.uppercase() }
                }
            } else {
                // Available order
                btnAccept.visibility = View.VISIBLE
                tvStatus.visibility = View.GONE
                btnAccept.setOnClickListener { onAcceptClick(order) }
            }

            // Call button
            btnCall.setOnClickListener {
                val phone = order.customerPhone ?: order.bakerPhone
                if (!phone.isNullOrEmpty()) {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                    itemView.context.startActivity(intent)
                }
            }

            // Entire card click
            itemView.setOnClickListener { onViewClick(order) }
        }
    }
}
