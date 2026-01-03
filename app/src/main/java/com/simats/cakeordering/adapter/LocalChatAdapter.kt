package com.simats.cakeordering.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.simats.cakeordering.R
import com.simats.cakeordering.model.LocalChatMessage

/**
 * Adapter for local chat messages - uses local URIs for images
 * @param isBakerView if true, baker messages appear on RIGHT; if false, customer messages appear on RIGHT
 */
class LocalChatAdapter(
    private var messages: MutableList<LocalChatMessage>,
    private val isBakerView: Boolean = true  // true for baker screen, false for customer screen
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_SENT = 1      // My messages (right)
        private const val VIEW_TYPE_RECEIVED = 2  // Other's messages (left)
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
        val message = messages[position]
        // For baker view: baker = sent (right), customer = received (left)
        // For customer view: customer = sent (right), baker = received (left)
        return if (isBakerView) {
            if (message.senderType == "baker") VIEW_TYPE_SENT else VIEW_TYPE_RECEIVED
        } else {
            if (message.senderType == "customer") VIEW_TYPE_SENT else VIEW_TYPE_RECEIVED
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
            is SentViewHolder -> bindMessage(holder.txtMessage, holder.txtTime, holder.imgMessage, message)
            is ReceivedViewHolder -> bindMessage(holder.txtMessage, holder.txtTime, holder.imgMessage, message)
        }
    }

    private fun bindMessage(txtMessage: TextView, txtTime: TextView, imgMessage: ImageView, message: LocalChatMessage) {
        // Handle text
        if (!message.message.isNullOrEmpty()) {
            txtMessage.text = message.message
            txtMessage.visibility = View.VISIBLE
        } else {
            txtMessage.visibility = View.GONE
        }
        
        txtTime.text = message.getTimeString()

        // Handle image - load directly from local URI
        if (message.imageUri != null) {
            imgMessage.visibility = View.VISIBLE
            Glide.with(imgMessage.context)
                .load(message.imageUri)
                .placeholder(R.drawable.ic_image)
                .error(R.drawable.ic_image)
                .into(imgMessage)
        } else {
            imgMessage.visibility = View.GONE
        }
    }

    override fun getItemCount() = messages.size

    fun updateData(newMessages: List<LocalChatMessage>) {
        messages.clear()
        messages.addAll(newMessages)
        notifyDataSetChanged()
    }

    fun addMessage(message: LocalChatMessage) {
        messages.add(message)
        notifyItemInserted(messages.size - 1)
    }
}
