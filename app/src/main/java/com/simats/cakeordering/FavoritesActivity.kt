package com.simats.cakeordering

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.simats.cakeordering.adapter.FavoritesAdapter
import com.simats.cakeordering.api.ApiClient
import com.simats.cakeordering.model.FavoriteResponse
import com.simats.cakeordering.model.FavoritesListResponse
import com.simats.cakeordering.model.FavoriteCake
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FavoritesActivity : AppCompatActivity() {

    private var userId: Int = 0
    private lateinit var rvFavorites: RecyclerView
    private lateinit var emptyState: LinearLayout
    private lateinit var tvSavedCount: TextView
    private lateinit var btnTabCakes: Button
    private lateinit var btnTabBakers: Button
    private var favoritesAdapter: FavoritesAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorites)

        userId = getSharedPreferences("CakeOrderingPrefs", MODE_PRIVATE)
            .getInt("user_id", 0)

        if (userId == 0) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupViews()
    }

    override fun onResume() {
        super.onResume()
        loadFavorites()
    }

    private fun setupViews() {
        rvFavorites = findViewById(R.id.rvFavorites)
        emptyState = findViewById(R.id.emptyState)
        tvSavedCount = findViewById(R.id.tvSavedCount)
        btnTabCakes = findViewById(R.id.btnTabCakes)
        btnTabBakers = findViewById(R.id.btnTabBakers)

        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        rvFavorites.layoutManager = LinearLayoutManager(this)

        // Tab click handlers
        btnTabCakes.setOnClickListener {
            selectTab(true)
        }

        btnTabBakers.setOnClickListener {
            selectTab(false)
            Toast.makeText(this, "Favorite Bakers - Coming Soon!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun selectTab(cakesSelected: Boolean) {
        if (cakesSelected) {
            btnTabCakes.setBackgroundColor(0xFFEC4899.toInt())
            btnTabCakes.setTextColor(0xFFFFFFFF.toInt())
            btnTabBakers.setBackgroundColor(0xFFF3F4F6.toInt())
            btnTabBakers.setTextColor(0xFF6B7280.toInt())
        } else {
            btnTabBakers.setBackgroundColor(0xFFEC4899.toInt())
            btnTabBakers.setTextColor(0xFFFFFFFF.toInt())
            btnTabCakes.setBackgroundColor(0xFFF3F4F6.toInt())
            btnTabCakes.setTextColor(0xFF6B7280.toInt())
        }
    }

    private fun loadFavorites() {
        ApiClient.api.getFavorites(userId)
            .enqueue(object : Callback<FavoritesListResponse> {
                override fun onResponse(
                    call: Call<FavoritesListResponse>,
                    response: Response<FavoritesListResponse>
                ) {
                    if (response.isSuccessful && response.body()?.status == "success") {
                        val favorites = response.body()?.favorites ?: emptyList()
                        val count = favorites.size
                        
                        tvSavedCount.text = "$count saved cakes"
                        btnTabCakes.text = "Cakes ($count)"
                        
                        if (favorites.isEmpty()) {
                            showEmpty()
                        } else {
                            showFavorites(favorites)
                        }
                    } else {
                        showEmpty()
                    }
                }

                override fun onFailure(call: Call<FavoritesListResponse>, t: Throwable) {
                    Toast.makeText(
                        this@FavoritesActivity,
                        "Connection error: ${t.message}",
                        Toast.LENGTH_LONG
                    ).show()
                    showEmpty()
                }
            })
    }

    private fun showEmpty() {
        rvFavorites.visibility = View.GONE
        emptyState.visibility = View.VISIBLE
        tvSavedCount.text = "0 saved cakes"
        btnTabCakes.text = "Cakes (0)"
    }

    private fun showFavorites(favorites: List<FavoriteCake>) {
        rvFavorites.visibility = View.VISIBLE
        emptyState.visibility = View.GONE

        favoritesAdapter = FavoritesAdapter(
            favorites.toMutableList(),
            onViewDetailsClick = { cake ->
                val intent = Intent(this, CakeDetailsActivity::class.java)
                intent.putExtra("cake_id", cake.cakeId)
                startActivity(intent)
            },
            onDeleteClick = { cake, position ->
                showDeleteConfirmation(cake, position)
            }
        )
        rvFavorites.adapter = favoritesAdapter
    }

    private fun showDeleteConfirmation(cake: FavoriteCake, position: Int) {
        AlertDialog.Builder(this)
            .setTitle("Remove from Favorites")
            .setMessage("Remove ${cake.cakeName} from your favorites?")
            .setPositiveButton("Remove") { _, _ ->
                removeFavorite(cake, position)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun removeFavorite(cake: FavoriteCake, position: Int) {
        ApiClient.api.toggleFavorite(userId, cake.cakeId, "remove")
            .enqueue(object : Callback<FavoriteResponse> {
                override fun onResponse(
                    call: Call<FavoriteResponse>,
                    response: Response<FavoriteResponse>
                ) {
                    if (response.isSuccessful && response.body()?.status == "success") {
                        favoritesAdapter?.removeItem(position)
                        
                        // Update count
                        val newCount = (favoritesAdapter?.itemCount ?: 0)
                        tvSavedCount.text = "$newCount saved cakes"
                        btnTabCakes.text = "Cakes ($newCount)"
                        
                        if (newCount == 0) {
                            showEmpty()
                        }
                        
                        Toast.makeText(
                            this@FavoritesActivity,
                            "Removed from favorites",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<FavoriteResponse>, t: Throwable) {
                    Toast.makeText(
                        this@FavoritesActivity,
                        "Error: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }
}
