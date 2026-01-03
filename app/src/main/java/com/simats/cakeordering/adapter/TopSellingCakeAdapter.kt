package com.simats.cakeordering.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.simats.cakeordering.R
import com.simats.cakeordering.model.TopCake

class TopSellingCakeAdapter(
    private var cakes: List<TopCake>
) : RecyclerView.Adapter<TopSellingCakeAdapter.CakeViewHolder>() {

    class CakeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtRank: TextView = view.findViewById(R.id.txtRank)
        val txtCakeName: TextView = view.findViewById(R.id.txtCakeName)
        val txtOrderCount: TextView = view.findViewById(R.id.txtOrderCount)
        val txtRevenue: TextView = view.findViewById(R.id.txtRevenue)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CakeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_top_cake, parent, false)
        return CakeViewHolder(view)
    }

    override fun onBindViewHolder(holder: CakeViewHolder, position: Int) {
        val cake = cakes[position]
        val rank = position + 1

        holder.txtRank.text = rank.toString()
        holder.txtCakeName.text = cake.cake_name
        holder.txtOrderCount.text = "${cake.order_count} orders"
        holder.txtRevenue.text = "₹${cake.revenue}"

        // Set rank badge background based on position
        val badgeBackground = when (rank) {
            1 -> R.drawable.bg_rank_1
            2 -> R.drawable.bg_rank_2
            3 -> R.drawable.bg_rank_3
            else -> R.drawable.bg_rank_4
        }
        holder.txtRank.setBackgroundResource(badgeBackground)
    }

    override fun getItemCount(): Int = cakes.size

    fun updateData(newList: List<TopCake>) {
        cakes = newList
        notifyDataSetChanged()
    }
}
