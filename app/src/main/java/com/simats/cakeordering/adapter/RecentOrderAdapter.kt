package com.simats.cakeordering.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.simats.cakeordering.R
import com.simats.cakeordering.model.RecentOrder

class RecentOrderAdapter(
    private val orders: List<RecentOrder>
) : RecyclerView.Adapter<RecentOrderAdapter.OrderViewHolder>() {

    class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtCustomerName: TextView = itemView.findViewById(R.id.txtCustomerName)
        val txtCakeName: TextView = itemView.findViewById(R.id.txtCakeName)
        val txtOrderAmount: TextView = itemView.findViewById(R.id.txtOrderAmount)
        val txtStatus: TextView = itemView.findViewById(R.id.txtStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recent_order, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orders[position]

        holder.txtCustomerName.text = order.customer_name
        holder.txtCakeName.text = order.cake_name
        holder.txtOrderAmount.text = "₹${order.amount}"
        holder.txtStatus.text = order.status
    }

    override fun getItemCount(): Int = orders.size
}
