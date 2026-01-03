package com.simats.cakeordering.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.simats.cakeordering.R
import com.simats.cakeordering.model.BakerChat

class CustomerBakerChatAdapter(
    private val bakers: List<BakerChat>,
    private val onBakerClick: (BakerChat) -> Unit
) : RecyclerView.Adapter<CustomerBakerChatAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgBaker: ImageView = view.findViewById(R.id.imgBaker)
        val tvBakerName: TextView = view.findViewById(R.id.tvBakerName)
        val tvLastMessage: TextView = view.findViewById(R.id.tvLastMessage)
        val tvTime: TextView = view.findViewById(R.id.tvTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_baker_chat, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val baker = bakers[position]

        holder.tvBakerName.text = baker.shopName ?: "Baker"
        holder.tvLastMessage.text = baker.lastMessage ?: "No messages yet"
        holder.tvTime.text = baker.lastMessageTime ?: ""

        // Load baker image if available
        if (!baker.shopImage.isNullOrEmpty()) {
            val imageUrl = if (baker.shopImage.startsWith("http")) {
                baker.shopImage
            } else {
                "https://zgt68nw9-80.inc1.devtunnels.ms/Custom-Cake-Ordering/${baker.shopImage}"
            }
            Glide.with(holder.itemView.context)
                .load(imageUrl)
                .placeholder(R.drawable.ic_person)
                .error(R.drawable.ic_person)
                .circleCrop()
                .into(holder.imgBaker)
        }

        holder.itemView.setOnClickListener {
            onBakerClick(baker)
        }
    }

    override fun getItemCount(): Int = bakers.size
}
