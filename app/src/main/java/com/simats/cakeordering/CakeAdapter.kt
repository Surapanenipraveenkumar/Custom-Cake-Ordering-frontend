package com.simats.cakeordering.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.simats.cakeordering.CakeDetailsActivity
import com.simats.cakeordering.databinding.ItemCakeBinding
import com.simats.cakeordering.model.Cake

class CakeAdapter(private var list: List<Cake>) :
    RecyclerView.Adapter<CakeAdapter.ViewHolder>() {

    companion object {
        private const val BASE_URL = "https://zgt68nw9-80.inc1.devtunnels.ms/Custom-Cake-Ordering/"
    }

    inner class ViewHolder(val binding: ItemCakeBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCakeBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val cake = list[position]

        holder.binding.tvCakeName.text = cake.name ?: "Unknown Cake"
        holder.binding.tvBakerName.text = cake.baker ?: "Unknown Baker"
        holder.binding.tvPrice.text = "₹${cake.price ?: "0"}"

        // Load cake image with full URL
        if (!cake.image.isNullOrEmpty()) {
            val fullImageUrl = BASE_URL + cake.image
            Glide.with(holder.itemView.context)
                .load(fullImageUrl)
                .into(holder.binding.ivCakeImage)
        }

        holder.binding.btnViewDetails.setOnClickListener {
            val intent = Intent(
                holder.itemView.context,
                CakeDetailsActivity::class.java
            )
            intent.putExtra("cake_id", cake.cake_id)
            intent.putExtra("baker_id", cake.baker_id)   // ✅ Pass baker_id
            intent.putExtra("baker_name", cake.baker)     // ✅ Pass baker name
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount() = list.size

    fun updateData(newList: List<Cake>) {
        list = newList
        notifyDataSetChanged()
    }
}
