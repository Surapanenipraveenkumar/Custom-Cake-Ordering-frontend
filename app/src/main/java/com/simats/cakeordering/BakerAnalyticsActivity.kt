package com.simats.cakeordering

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.simats.cakeordering.adapter.MonthlyTrendAdapter
import com.simats.cakeordering.adapter.TopSellingCakeAdapter
import com.simats.cakeordering.api.ApiClient
import com.simats.cakeordering.model.BakerAnalyticsResponse
import com.simats.cakeordering.model.RatingDistribution
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BakerAnalyticsActivity : AppCompatActivity() {

    private var bakerId: Int = 0
    private lateinit var trendAdapter: MonthlyTrendAdapter
    private lateinit var topCakesAdapter: TopSellingCakeAdapter
    
    // Auto-refresh handler for real-time updates
    private val refreshHandler = Handler(Looper.getMainLooper())
    private val refreshInterval = 30000L // 30 seconds
    private val refreshRunnable = object : Runnable {
        override fun run() {
            loadAnalytics()
            refreshHandler.postDelayed(this, refreshInterval)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_baker_analytics)

        bakerId = intent.getIntExtra("baker_id", 0)
        if (bakerId == 0) {
            Toast.makeText(this, "Invalid baker", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Back button
        findViewById<TextView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        // Setup RecyclerViews
        setupRecyclerViews()

        // Load analytics data
        loadAnalytics()
    }

    override fun onResume() {
        super.onResume()
        // Start auto-refresh for real-time updates
        refreshHandler.postDelayed(refreshRunnable, refreshInterval)
    }

    override fun onPause() {
        super.onPause()
        // Stop auto-refresh when activity is not visible
        refreshHandler.removeCallbacks(refreshRunnable)
    }

    private fun setupRecyclerViews() {
        // Monthly Trend RecyclerView
        trendAdapter = MonthlyTrendAdapter(emptyList())
        findViewById<RecyclerView>(R.id.rvMonthlyTrend).apply {
            layoutManager = LinearLayoutManager(this@BakerAnalyticsActivity)
            adapter = trendAdapter
        }

        // Top Cakes RecyclerView
        topCakesAdapter = TopSellingCakeAdapter(emptyList())
        findViewById<RecyclerView>(R.id.rvTopCakes).apply {
            layoutManager = LinearLayoutManager(this@BakerAnalyticsActivity)
            adapter = topCakesAdapter
        }
    }

    private fun loadAnalytics() {
        ApiClient.api.getBakerAnalytics(bakerId)
            .enqueue(object : Callback<BakerAnalyticsResponse> {
                override fun onResponse(
                    call: Call<BakerAnalyticsResponse>,
                    response: Response<BakerAnalyticsResponse>
                ) {
                    if (response.isSuccessful && response.body()?.status == "success") {
                        val data = response.body()!!
                        updateUI(data)
                    } else {
                        Toast.makeText(
                            this@BakerAnalyticsActivity,
                            "Failed to load analytics",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<BakerAnalyticsResponse>, t: Throwable) {
                    Toast.makeText(
                        this@BakerAnalyticsActivity,
                        "Error: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun updateUI(data: BakerAnalyticsResponse) {
        // Today Stats
        findViewById<TextView>(R.id.txtTodayIncome).text = "₹${data.today.income}"
        findViewById<TextView>(R.id.txtTodayOrders).text = "${data.today.orders} orders"

        // This Week Stats
        findViewById<TextView>(R.id.txtWeekIncome).text = "₹${data.thisWeek.income}"
        findViewById<TextView>(R.id.txtWeekOrders).text = "${data.thisWeek.orders} orders"

        // This Month Stats
        findViewById<TextView>(R.id.txtMonthIncome).text = "₹${data.thisMonth.income}"
        findViewById<TextView>(R.id.txtMonthOrders).text = "${data.thisMonth.orders} orders completed"
        
        // Percent Change
        val percentText = if (data.thisMonth.percentChange >= 0) {
            "+${data.thisMonth.percentChange}%"
        } else {
            "${data.thisMonth.percentChange}%"
        }
        findViewById<TextView>(R.id.txtPercentChange).text = percentText

        // Order Statistics
        findViewById<TextView>(R.id.txtStatTotal).text = data.orderStats.total.toString()
        findViewById<TextView>(R.id.txtStatPending).text = data.orderStats.pending.toString()
        findViewById<TextView>(R.id.txtStatCompleted).text = data.orderStats.completed.toString()

        // 6-Month Trend
        trendAdapter.updateData(data.monthlyTrend)

        // Top Selling Cakes
        topCakesAdapter.updateData(data.topCakes)

        // Customer Ratings
        updateRatingsUI(data.ratings.average, data.ratings.total_reviews, data.ratings.distribution)
    }

    private fun updateRatingsUI(average: Double, totalReviews: Int, distribution: List<RatingDistribution>) {
        findViewById<TextView>(R.id.txtAverageRating).text = String.format("%.1f", average)
        findViewById<TextView>(R.id.txtTotalReviews).text = "Based on $totalReviews reviews"

        // Build rating distribution bars
        val container = findViewById<LinearLayout>(R.id.ratingDistributionContainer)
        container.removeAllViews()

        val maxCount = distribution.maxOfOrNull { it.count } ?: 1

        distribution.forEach { rating ->
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = 8
                }
            }

            // Stars label
            val starsLabel = TextView(this).apply {
                text = "${rating.stars} ★"
                textSize = 12f
                setTextColor(Color.parseColor("#F59E0B"))
                layoutParams = LinearLayout.LayoutParams(50, LinearLayout.LayoutParams.WRAP_CONTENT)
            }
            row.addView(starsLabel)

            // Progress bar container
            val progressContainer = LinearLayout(this).apply {
                layoutParams = LinearLayout.LayoutParams(0, 12).apply {
                    weight = 1f
                    marginStart = 12
                    marginEnd = 12
                }
                setBackgroundResource(R.drawable.bg_progress_bar)
            }

            // Progress fill
            val progressPercent = if (maxCount > 0) rating.count.toFloat() / maxCount.toFloat() else 0f
            val progressFill = View(this).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT).apply {
                    // Width will be set after layout
                }
                setBackgroundColor(Color.parseColor("#F59E0B"))
            }
            progressContainer.addView(progressFill)
            
            // Set progress width after layout
            progressContainer.post {
                val params = progressFill.layoutParams
                params.width = (progressContainer.width * progressPercent).toInt()
                progressFill.layoutParams = params
            }
            
            row.addView(progressContainer)

            // Count label
            val countLabel = TextView(this).apply {
                text = rating.count.toString()
                textSize = 12f
                setTextColor(Color.parseColor("#6B7280"))
                layoutParams = LinearLayout.LayoutParams(40, LinearLayout.LayoutParams.WRAP_CONTENT)
                gravity = Gravity.END
            }
            row.addView(countLabel)

            container.addView(row)
        }
    }
}
