package com.simats.cakeordering

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.simats.cakeordering.adapter.AddressAdapter
import com.simats.cakeordering.api.ApiClient
import com.simats.cakeordering.databinding.ActivityCheckoutBinding
import com.simats.cakeordering.model.Address
import com.simats.cakeordering.model.AddressResponse
import com.simats.cakeordering.model.BasicResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class CheckoutActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "CheckoutActivity"
    }

    private lateinit var binding: ActivityCheckoutBinding
    private lateinit var addressAdapter: AddressAdapter
    private var isDelivery = false
    private var subtotal = 0.0
    private var deliveryFee = 0
    private var selectedDate: String = ""
    private var selectedTime: String = ""
    private var selectedAddress: Address? = null
    private var orderItems: ArrayList<String> = arrayListOf()
    private var orderPrices: ArrayList<Int> = arrayListOf()
    private var userId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCheckoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get user ID
        val prefs = getSharedPreferences("CakeOrderingPrefs", Context.MODE_PRIVATE)
        userId = prefs.getInt("user_id", 0)

        // Get data from intent
        subtotal = intent.getDoubleExtra("subtotal", 0.0)
        orderItems = intent.getStringArrayListExtra("items") ?: arrayListOf()
        orderPrices = intent.getIntegerArrayListExtra("prices") ?: arrayListOf()

        setupAddressRecycler()
        setupClickListeners()
        updateDeliverySelection()
        displayOrderItems()
        updatePrices()
    }

    private fun setupAddressRecycler() {
        addressAdapter = AddressAdapter(
            addresses = mutableListOf(),
            onAddressSelected = { address ->
                selectedAddress = address
                Log.d(TAG, "Selected address: ${address.fullAddress}")
            }
        )
        binding.recyclerAddresses.layoutManager = LinearLayoutManager(this)
        binding.recyclerAddresses.adapter = addressAdapter
    }

    private fun setupClickListeners() {
        // Back button
        binding.btnBack.setOnClickListener {
            finish()
        }

        // Delivery option
        binding.optionDelivery.setOnClickListener {
            isDelivery = true
            updateDeliverySelection()
            updatePrices()
            loadAddresses()
        }

        // Pickup option
        binding.optionPickup.setOnClickListener {
            isDelivery = false
            updateDeliverySelection()
            updatePrices()
        }

        // Add new address
        binding.btnAddAddress.setOnClickListener {
            showAddAddressDialog()
        }

        // Date picker
        binding.btnSelectDate.setOnClickListener {
            showDatePicker()
        }

        // Time picker
        binding.btnSelectTime.setOnClickListener {
            showTimePicker()
        }

        // Proceed to payment
        binding.btnPayment.setOnClickListener {
            processPayment()
        }
    }

    private fun updateDeliverySelection() {
        if (isDelivery) {
            binding.optionDelivery.setBackgroundResource(R.drawable.bg_delivery_option_selected)
            binding.optionPickup.setBackgroundResource(R.drawable.bg_delivery_option)
            binding.addressSection.visibility = View.VISIBLE
            deliveryFee = 50
        } else {
            binding.optionDelivery.setBackgroundResource(R.drawable.bg_delivery_option)
            binding.optionPickup.setBackgroundResource(R.drawable.bg_delivery_option_selected)
            binding.addressSection.visibility = View.GONE
            deliveryFee = 0
        }
    }

    private fun loadAddresses() {
        if (userId == 0) return

        binding.addressProgress.visibility = View.VISIBLE
        binding.recyclerAddresses.visibility = View.GONE
        binding.tvNoAddresses.visibility = View.GONE

        ApiClient.api.getAddresses(userId)
            .enqueue(object : Callback<AddressResponse> {
                override fun onResponse(
                    call: Call<AddressResponse>,
                    response: Response<AddressResponse>
                ) {
                    binding.addressProgress.visibility = View.GONE

                    if (response.isSuccessful && response.body()?.status == "success") {
                        val addresses = response.body()!!.addresses
                        Log.d(TAG, "Loaded ${addresses.size} addresses")

                        if (addresses.isEmpty()) {
                            binding.tvNoAddresses.visibility = View.VISIBLE
                            binding.recyclerAddresses.visibility = View.GONE
                        } else {
                            binding.tvNoAddresses.visibility = View.GONE
                            binding.recyclerAddresses.visibility = View.VISIBLE
                            addressAdapter.updateAddresses(addresses)
                        }
                    } else {
                        binding.tvNoAddresses.visibility = View.VISIBLE
                    }
                }

                override fun onFailure(call: Call<AddressResponse>, t: Throwable) {
                    binding.addressProgress.visibility = View.GONE
                    binding.tvNoAddresses.visibility = View.VISIBLE
                    Log.e(TAG, "Failed to load addresses", t)
                }
            })
    }

    private fun showAddAddressDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_add_address)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        val etFullAddress = dialog.findViewById<EditText>(R.id.etFullAddress)
        val etPincode = dialog.findViewById<EditText>(R.id.etPincode)
        val etLandmark = dialog.findViewById<EditText>(R.id.etLandmark)
        val etPhone = dialog.findViewById<EditText>(R.id.etPhone)
        val cbSetDefault = dialog.findViewById<CheckBox>(R.id.cbSetDefault)
        val chipHome = dialog.findViewById<Chip>(R.id.chipHome)
        val chipWork = dialog.findViewById<Chip>(R.id.chipWork)
        val chipOther = dialog.findViewById<Chip>(R.id.chipOther)
        val btnCancel = dialog.findViewById<Button>(R.id.btnCancel)
        val btnSave = dialog.findViewById<Button>(R.id.btnSaveAddress)

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnSave.setOnClickListener {
            val address = etFullAddress.text.toString().trim()
            if (address.isEmpty()) {
                etFullAddress.error = "Please enter your address"
                return@setOnClickListener
            }

            val pincode = etPincode.text.toString().trim()
            if (pincode.isEmpty() || pincode.length != 6) {
                etPincode.error = "Please enter a valid 6-digit pincode"
                return@setOnClickListener
            }

            val label = when {
                chipWork.isChecked -> "Work"
                chipOther.isChecked -> "Other"
                else -> "Home"
            }

            val body = mapOf(
                "user_id" to userId,
                "label" to label,
                "full_address" to address,
                "pincode" to pincode,
                "landmark" to etLandmark.text.toString().trim(),
                "phone" to etPhone.text.toString().trim(),
                "is_default" to if (cbSetDefault.isChecked) 1 else 0
            )

            saveAddress(body, dialog)
        }

        dialog.show()
    }

    private fun saveAddress(body: Map<String, Any>, dialog: Dialog) {
        Toast.makeText(this, "Saving address...", Toast.LENGTH_SHORT).show()

        ApiClient.api.addAddress(body)
            .enqueue(object : Callback<BasicResponse> {
                override fun onResponse(
                    call: Call<BasicResponse>,
                    response: Response<BasicResponse>
                ) {
                    if (response.isSuccessful && response.body()?.status == "success") {
                        Toast.makeText(this@CheckoutActivity, "Address saved!", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                        loadAddresses()
                    } else {
                        Toast.makeText(
                            this@CheckoutActivity,
                            response.body()?.message ?: "Failed to save",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<BasicResponse>, t: Throwable) {
                    Toast.makeText(this@CheckoutActivity, "Connection error", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun displayOrderItems() {
        binding.orderItemsContainer.removeAllViews()

        for (i in orderItems.indices) {
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = 8
                }
            }

            val nameView = TextView(this).apply {
                text = orderItems.getOrNull(i) ?: "Item"
                textSize = 14f
                setTextColor(resources.getColor(android.R.color.darker_gray, null))
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }

            val priceView = TextView(this).apply {
                text = "₹${orderPrices.getOrNull(i) ?: 0}"
                textSize = 14f
                setTextColor(resources.getColor(android.R.color.black, null))
            }

            row.addView(nameView)
            row.addView(priceView)
            binding.orderItemsContainer.addView(row)
        }
    }

    private fun updatePrices() {
        val total = subtotal.toInt() + deliveryFee

        binding.tvSubtotal.text = "₹${subtotal.toInt()}"
        binding.tvDeliveryFee.text = "₹$deliveryFee"
        binding.tvTotal.text = "₹$total"
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePicker = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val date = Calendar.getInstance()
                date.set(selectedYear, selectedMonth, selectedDay)
                val format = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                selectedDate = format.format(date.time)
                binding.tvSelectedDate.text = selectedDate
                binding.tvSelectedDate.setTextColor(resources.getColor(android.R.color.black, null))
            },
            year, month, day
        )

        datePicker.datePicker.minDate = System.currentTimeMillis() - 1000
        datePicker.show()
    }

    private fun showTimePicker() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        TimePickerDialog(
            this,
            { _, selectedHour, selectedMinute ->
                val time = Calendar.getInstance()
                time.set(Calendar.HOUR_OF_DAY, selectedHour)
                time.set(Calendar.MINUTE, selectedMinute)
                val format = SimpleDateFormat("hh:mm a", Locale.getDefault())
                selectedTime = format.format(time.time)
                binding.tvSelectedTime.text = selectedTime
                binding.tvSelectedTime.setTextColor(resources.getColor(android.R.color.black, null))
            },
            hour, minute, false
        ).show()
    }

    private fun processPayment() {
        if (selectedDate.isEmpty()) {
            Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedTime.isEmpty()) {
            Toast.makeText(this, "Please select a time", Toast.LENGTH_SHORT).show()
            return
        }

        if (isDelivery && selectedAddress == null) {
            Toast.makeText(this, "Please select a delivery address", Toast.LENGTH_SHORT).show()
            return
        }

        // Navigate to Payment screen
        val intent = Intent(this, PaymentActivity::class.java).apply {
            putExtra("subtotal", subtotal.toInt())
            putExtra("delivery_fee", deliveryFee)
            putExtra("item_name", orderItems.joinToString(", "))
            putExtra("delivery_address", selectedAddress?.fullAddress ?: "Pickup from store")
            putExtra("delivery_date", selectedDate)
            putExtra("delivery_time", selectedTime)
        }
        startActivity(intent)
    }
}
