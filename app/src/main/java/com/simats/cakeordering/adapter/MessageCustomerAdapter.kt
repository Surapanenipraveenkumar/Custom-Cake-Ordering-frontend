package com.simats.cakeordering.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.simats.cakeordering.R
import com.simats.cakeordering.model.MessageCustomer

class MessageCustomerAdapter(
    private var customers: MutableList<MessageCustomer>,
    private val onItemClick: (MessageCustomer) -> Unit
) : RecyclerView.Adapter<MessageCustomerAdapter.CustomerViewHolder>() {

    companion object {
        private const val BASE_URL = "https://zgt68nw9-80.inc1.devtunnels.ms/Custom-Cake-Ordering/"
    }

    inner class CustomerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgAvatar: ImageView = view.findViewById(R.id.imgAvatar)
        val txtCustomerName: TextView = view.findViewById(R.id.txtCustomerName)
        val txtLastMessage: TextView = view.findViewById(R.id.txtLastMessage)
        val txtTimeAgo: TextView = view.findViewById(R.id.txtTimeAgo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_message_customer, parent, false)
        return CustomerViewHolder(view)
    }

    override fun onBindViewHolder(holder: CustomerViewHolder, position: Int) {
        val customer = customers[position]
        
        holder.txtCustomerName.text = customer.name
        holder.txtLastMessage.text = customer.last_message ?: "Click to open chat"
        holder.txtTimeAgo.text = customer.time_ago
        
        // Load profile image if available
        if (!customer.profile_image.isNullOrEmpty()) {
            val imageUrl = if (customer.profile_image.startsWith("http")) {
                customer.profile_image
            } else {
                BASE_URL + customer.profile_image
            }
            Glide.with(holder.itemView.context)
                .load(imageUrl)
                .placeholder(R.drawable.ic_profile)
                .error(R.drawable.ic_profile)
                .circleCrop()
                .into(holder.imgAvatar)
        }
        
        holder.itemView.setOnClickListener {
            onItemClick(customer)
        }
    }

    override fun getItemCount() = customers.size

    fun updateData(newCustomers: List<MessageCustomer>) {
        customers.clear()
        customers.addAll(newCustomers)
        notifyDataSetChanged()
    }
}
