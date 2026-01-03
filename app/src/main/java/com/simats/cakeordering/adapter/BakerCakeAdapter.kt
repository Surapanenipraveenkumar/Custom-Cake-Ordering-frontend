package com.simats.cakeordering.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.simats.cakeordering.ManageCakeActivity
import com.simats.cakeordering.R
import com.simats.cakeordering.model.BakerCake

class BakerCakeAdapter(
    private var cakes: List<BakerCake>,
    private val bakerId: Int
) : RecyclerView.Adapter<BakerCakeAdapter.CakeViewHolder>() {

    companion object {
        private const val BASE_URL = "https://zgt68nw9-80.inc1.devtunnels.ms/Custom-Cake-Ordering/"
    }

    class CakeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgCake: ImageView = view.findViewById(R.id.imgCake)
        val txtName: TextView = view.findViewById(R.id.txtCakeName)
        val txtPrice: TextView = view.findViewById(R.id.txtCakePrice)
        val txtOrders: TextView = view.findViewById(R.id.txtOrderCount)
        val btnManage: Button = view.findViewById(R.id.btnManage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CakeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_baker_cake, parent, false)
        return CakeViewHolder(view)
    }

    override fun onBindViewHolder(holder: CakeViewHolder, position: Int) {
        val cake = cakes[position]

        // Load cake image with Glide
        if (!cake.image.isNullOrEmpty()) {
            val fullImageUrl = when {
                cake.image.startsWith("http") -> cake.image
                cake.image.startsWith("/") -> BASE_URL + cake.image.substring(1)
                else -> BASE_URL + cake.image
            }
            Glide.with(holder.itemView.context)
                .load(fullImageUrl)
                .placeholder(R.drawable.ic_placeholder_image)
                .error(R.drawable.ic_placeholder_image)
                .centerCrop()
                .into(holder.imgCake)
        } else {
            holder.imgCake.setImageResource(R.drawable.ic_placeholder_image)
        }

        holder.txtName.text = cake.cake_name
        holder.txtPrice.text = "₹${cake.price}"
        holder.txtOrders.text = "${cake.orders} orders"

        // Manage button click
        holder.btnManage.setOnClickListener {
            val context = holder.itemView.context
            context.startActivity(
                Intent(context, ManageCakeActivity::class.java)
                    .putExtra("cake_id", cake.cake_id)
                    .putExtra("baker_id", bakerId)
            )
        }
    }

    override fun getItemCount(): Int = cakes.size

    fun updateData(newList: List<BakerCake>) {
        cakes = newList
        notifyDataSetChanged()
    }
}
