package com.simats.cakeordering.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.simats.cakeordering.BakerDetailsActivity
import com.simats.cakeordering.R
import com.simats.cakeordering.model.NearbyBaker

class NearbyBakerAdapter(
    private var bakers: List<NearbyBaker>,
    private val onChatClick: (NearbyBaker) -> Unit
) : RecyclerView.Adapter<NearbyBakerAdapter.BakerViewHolder>() {

    companion object {
        private const val BASE_URL = "https://zgt68nw9-80.inc1.devtunnels.ms/Custom-Cake-Ordering/"
    }

    inner class BakerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgBaker: ImageView = view.findViewById(R.id.imgBaker)
        val txtShopName: TextView = view.findViewById(R.id.txtShopName)
        val txtRating: TextView = view.findViewById(R.id.txtRating)
        val txtReviewCount: TextView = view.findViewById(R.id.txtReviewCount)
        val txtAddress: TextView = view.findViewById(R.id.txtAddress)
        val txtDistance: TextView = view.findViewById(R.id.txtDistance)
        val txtSpecialty: TextView = view.findViewById(R.id.txtSpecialty)
        val txtExperience: TextView = view.findViewById(R.id.txtExperience)
        val btnChat: ImageView = view.findViewById(R.id.btnChat)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BakerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_nearby_baker, parent, false)
        return BakerViewHolder(view)
    }

    override fun onBindViewHolder(holder: BakerViewHolder, position: Int) {
        val baker = bakers[position]

        holder.txtShopName.text = baker.shopName
        holder.txtRating.text = String.format("%.1f", baker.rating)
        holder.txtReviewCount.text = "(${baker.reviewCount})"
        holder.txtAddress.text = baker.address ?: "Location not set"
        holder.txtDistance.text = "${baker.distance} km"
        holder.txtSpecialty.text = baker.specialty ?: "Custom Cakes"
        
        val expText = if (baker.yearsExperience > 0) {
            "${baker.yearsExperience} years exp"
        } else {
            "New baker"
        }
        holder.txtExperience.text = expText

        // Load baker image
        if (!baker.profileImage.isNullOrEmpty()) {
            val imageUrl = when {
                baker.profileImage.startsWith("http") -> baker.profileImage
                baker.profileImage.startsWith("/") -> BASE_URL + baker.profileImage.substring(1)
                else -> BASE_URL + baker.profileImage
            }
            Glide.with(holder.itemView.context)
                .load(imageUrl)
                .placeholder(R.drawable.ic_placeholder_image)
                .error(R.drawable.ic_placeholder_image)
                .centerCrop()
                .into(holder.imgBaker)
        } else {
            holder.imgBaker.setImageResource(R.drawable.ic_placeholder_image)
        }

        // Item click - navigate to BakerDetailsActivity
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, BakerDetailsActivity::class.java)
            intent.putExtra("baker_id", baker.bakerId)
            intent.putExtra("shop_name", baker.shopName)
            context.startActivity(intent)
        }

        // Chat button click
        holder.btnChat.setOnClickListener {
            onChatClick(baker)
        }
    }

    override fun getItemCount() = bakers.size

    fun updateData(newBakers: List<NearbyBaker>) {
        bakers = newBakers
        notifyDataSetChanged()
    }
}
