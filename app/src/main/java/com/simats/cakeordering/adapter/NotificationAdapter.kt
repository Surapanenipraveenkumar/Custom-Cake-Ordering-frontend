package com.simats.cakeordering.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.simats.cakeordering.R
import com.simats.cakeordering.model.NotificationItem

class NotificationAdapter(
    private val onItemClick: (NotificationItem) -> Unit
) : RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {

    private val items = mutableListOf<NotificationItem>()

    fun submitList(newItems: List<NotificationItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val container: LinearLayout = view.findViewById(R.id.notificationContainer)
        private val tvIcon: TextView = view.findViewById(R.id.tvIcon)
        private val tvTitle: TextView = view.findViewById(R.id.tvTitle)
        private val tvMessage: TextView = view.findViewById(R.id.tvMessage)
        private val tvTime: TextView = view.findViewById(R.id.tvTime)
        private val unreadDot: View = view.findViewById(R.id.unreadDot)

        fun bind(item: NotificationItem) {
            tvTitle.text = item.title
            tvMessage.text = item.message ?: ""
            tvTime.text = item.timeAgo
            
            // Set icon based on type
            tvIcon.text = getIconForType(item.type)
            
            // Show unread dot
            unreadDot.visibility = if (!item.isRead) View.VISIBLE else View.GONE
            
            // Set background for unread
            if (!item.isRead) {
                container.setBackgroundColor(0xFFFEF3C7.toInt())
            } else {
                container.setBackgroundColor(0xFFFFFFFF.toInt())
            }
            
            container.setOnClickListener {
                onItemClick(item)
            }
        }

        private fun getIconForType(type: String): String {
            return when (type.lowercase()) {
                // Customer notifications
                "order_placed" -> "📦"
                "order_confirmed" -> "✅"
                "preparing" -> "👨‍🍳"
                "out_for_delivery" -> "🚗"
                "delivered" -> "🎉"
                "chat" -> "💬"
                "payment" -> "💳"
                
                // Baker notifications
                "new_order" -> "🛒"
                "delivery_accepted" -> "🤝"
                "order_picked_up" -> "📤"
                
                // Delivery notifications
                "order_available" -> "📍"
                "order_delivered" -> "✅"
                "rating_received" -> "⭐"
                
                else -> "🔔"
            }
        }
    }
}
