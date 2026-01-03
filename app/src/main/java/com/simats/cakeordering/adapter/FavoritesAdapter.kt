package com.simats.cakeordering.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.simats.cakeordering.R
import com.simats.cakeordering.model.FavoriteCake

class FavoritesAdapter(
    private var favorites: MutableList<FavoriteCake>,
    private val onViewDetailsClick: (FavoriteCake) -> Unit,
    private val onDeleteClick: (FavoriteCake, Int) -> Unit
) : RecyclerView.Adapter<FavoritesAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgCake: ImageView = view.findViewById(R.id.imgCake)
        val tvCakeName: TextView = view.findViewById(R.id.tvCakeName)
        val tvBakerName: TextView = view.findViewById(R.id.tvBakerName)
        val tvRating: TextView = view.findViewById(R.id.tvRating)
        val tvPrice: TextView = view.findViewById(R.id.tvPrice)
        val tvAddedDate: TextView = view.findViewById(R.id.tvAddedDate)
        val btnDelete: ImageView = view.findViewById(R.id.btnDelete)
        val btnViewDetails: Button = view.findViewById(R.id.btnViewDetails)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_favorite_cake, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val cake = favorites[position]

        holder.tvCakeName.text = cake.cakeName ?: "Cake"
        holder.tvBakerName.text = cake.bakerName ?: "Baker"
        holder.tvRating.text = "4.8"  // Default rating, could be from API
        holder.tvPrice.text = "₹${cake.price.toInt()}"
        
        // Format date
        val addedDate = cake.favoritedAt?.take(10) ?: ""
        holder.tvAddedDate.text = if (addedDate.isNotEmpty()) "Added $addedDate" else ""

        // Load cake image
        if (!cake.image.isNullOrEmpty()) {
            val imageUrl = if (cake.image.startsWith("http")) {
                cake.image
            } else {
                "https://zgt68nw9-80.inc1.devtunnels.ms/Custom-Cake-Ordering/${cake.image}"
            }
            Glide.with(holder.itemView.context)
                .load(imageUrl)
                .placeholder(R.drawable.bg_circle_light)
                .error(R.drawable.bg_circle_light)
                .centerCrop()
                .into(holder.imgCake)
        }

        holder.btnViewDetails.setOnClickListener {
            onViewDetailsClick(cake)
        }

        holder.btnDelete.setOnClickListener {
            onDeleteClick(cake, position)
        }
    }

    override fun getItemCount(): Int = favorites.size

    fun removeItem(position: Int) {
        if (position >= 0 && position < favorites.size) {
            favorites.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, favorites.size)
        }
    }

    fun updateData(newFavorites: List<FavoriteCake>) {
        favorites = newFavorites.toMutableList()
        notifyDataSetChanged()
    }
}
