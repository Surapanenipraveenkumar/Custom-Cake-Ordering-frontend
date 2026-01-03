package com.simats.cakeordering.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.simats.cakeordering.R
import com.simats.cakeordering.model.ChatMessage

class ChatAdapter(
    private var messages: MutableList<ChatMessage>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_SENT = 1
        private const val VIEW_TYPE_RECEIVED = 2
        // Base URL for image loading
        private const val BASE_URL = "https://zgt68nw9-80.inc1.devtunnels.ms/Custom-Cake-Ordering/"
    }

    inner class SentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtMessage: TextView = view.findViewById(R.id.txtMessage)
        val txtTime: TextView = view.findViewById(R.id.txtTime)
        val imgMessage: ImageView = view.findViewById(R.id.imgMessage)
    }

    inner class ReceivedViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtMessage: TextView = view.findViewById(R.id.txtMessage)
        val txtTime: TextView = view.findViewById(R.id.txtTime)
        val imgMessage: ImageView = view.findViewById(R.id.imgMessage)
    }

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].sender_type == "baker") {
            VIEW_TYPE_SENT
        } else {
            VIEW_TYPE_RECEIVED
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_SENT) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_message_sent, parent, false)
            SentViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_message_received, parent, false)
            ReceivedViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]

        when (holder) {
            is SentViewHolder -> {
                // Handle text
                if (!message.message.isNullOrEmpty()) {
                    holder.txtMessage.text = message.message
                    holder.txtMessage.visibility = View.VISIBLE
                } else {
                    holder.txtMessage.visibility = View.GONE
                }
                
                holder.txtTime.text = message.time

                // Handle image
                if (!message.image_url.isNullOrEmpty()) {
                    holder.imgMessage.visibility = View.VISIBLE
                    // Prepend base URL if the image_url is a relative path
                    val fullImageUrl = if (message.image_url.startsWith("http")) {
                        message.image_url
                    } else {
                        BASE_URL + message.image_url
                    }
                    Glide.with(holder.itemView.context)
                        .load(fullImageUrl)
                        .placeholder(R.drawable.ic_image)
                        .error(R.drawable.ic_image)
                        .into(holder.imgMessage)
                } else {
                    holder.imgMessage.visibility = View.GONE
                }
            }
            is ReceivedViewHolder -> {
                // Handle text
                if (!message.message.isNullOrEmpty()) {
                    holder.txtMessage.text = message.message
                    holder.txtMessage.visibility = View.VISIBLE
                } else {
                    holder.txtMessage.visibility = View.GONE
                }
                
                holder.txtTime.text = message.time

                // Handle image
                if (!message.image_url.isNullOrEmpty()) {
                    holder.imgMessage.visibility = View.VISIBLE
                    // Prepend base URL if the image_url is a relative path
                    val fullImageUrl = if (message.image_url.startsWith("http")) {
                        message.image_url
                    } else {
                        BASE_URL + message.image_url
                    }
                    Glide.with(holder.itemView.context)
                        .load(fullImageUrl)
                        .placeholder(R.drawable.ic_image)
                        .error(R.drawable.ic_image)
                        .into(holder.imgMessage)
                } else {
                    holder.imgMessage.visibility = View.GONE
                }
            }
        }
    }

    override fun getItemCount() = messages.size

    fun updateData(newMessages: List<ChatMessage>) {
        messages.clear()
        messages.addAll(newMessages)
        notifyDataSetChanged()
    }

    fun addMessage(message: ChatMessage) {
        messages.add(message)
        notifyItemInserted(messages.size - 1)
    }
}
