package com.simats.cakeordering.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.simats.cakeordering.R
import com.simats.cakeordering.model.MonthTrend

class MonthlyTrendAdapter(
    private var trends: List<MonthTrend>
) : RecyclerView.Adapter<MonthlyTrendAdapter.TrendViewHolder>() {

    private var maxIncome: Int = 1

    class TrendViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtMonth: TextView = view.findViewById(R.id.txtMonth)
        val progressBar: View = view.findViewById(R.id.progressBar)
        val txtAmount: TextView = view.findViewById(R.id.txtAmount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrendViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_monthly_trend, parent, false)
        return TrendViewHolder(view)
    }

    override fun onBindViewHolder(holder: TrendViewHolder, position: Int) {
        val trend = trends[position]

        holder.txtMonth.text = trend.month
        holder.txtAmount.text = "₹${trend.income}"

        // Calculate progress bar width as percentage of max
        val progressPercent = if (maxIncome > 0) {
            (trend.income.toFloat() / maxIncome.toFloat())
        } else {
            0f
        }

        // Set progress bar width
        holder.progressBar.post {
            val parentWidth = (holder.progressBar.parent as View).width
            val newWidth = (parentWidth * progressPercent).toInt()
            val params = holder.progressBar.layoutParams
            params.width = newWidth.coerceAtLeast(1)
            holder.progressBar.layoutParams = params
        }
    }

    override fun getItemCount(): Int = trends.size

    fun updateData(newList: List<MonthTrend>) {
        trends = newList
        maxIncome = newList.maxOfOrNull { it.income } ?: 1
        notifyDataSetChanged()
    }
}
