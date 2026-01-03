package com.simats.cakeordering.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.simats.cakeordering.R
import com.simats.cakeordering.databinding.ItemAddressBinding
import com.simats.cakeordering.model.Address

class AddressAdapter(
    private var addresses: MutableList<Address>,
    private var selectedAddressId: Int = -1,
    private val onAddressSelected: (Address) -> Unit
) : RecyclerView.Adapter<AddressAdapter.AddressViewHolder>() {

    inner class AddressViewHolder(val binding: ItemAddressBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddressViewHolder {
        val binding = ItemAddressBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AddressViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AddressViewHolder, position: Int) {
        val address = addresses[position]
        
        with(holder.binding) {
            // Label
            tvLabel.text = address.label

            // Full address with pincode
            val addressWithPincode = if (!address.pincode.isNullOrEmpty()) {
                "${address.fullAddress} - ${address.pincode}"
            } else {
                address.fullAddress
            }
            tvFullAddress.text = addressWithPincode

            // Landmark
            if (!address.landmark.isNullOrEmpty()) {
                tvLandmark.text = "📍 ${address.landmark}"
                tvLandmark.visibility = View.VISIBLE
            } else {
                tvLandmark.visibility = View.GONE
            }

            // Phone
            if (!address.phone.isNullOrEmpty()) {
                tvPhone.text = "📞 ${address.phone}"
                tvPhone.visibility = View.VISIBLE
            } else {
                tvPhone.visibility = View.GONE
            }

            // Default badge
            tvDefault.visibility = if (address.isDefault == 1) View.VISIBLE else View.GONE

            // Selection state
            val isSelected = address.addressId == selectedAddressId
            if (isSelected) {
                addressCard.setBackgroundResource(R.drawable.bg_delivery_option_selected)
                ivSelected.visibility = View.VISIBLE
            } else {
                addressCard.setBackgroundResource(R.drawable.bg_delivery_option)
                ivSelected.visibility = View.GONE
            }

            // Click handler
            root.setOnClickListener {
                val previousSelected = selectedAddressId
                selectedAddressId = address.addressId
                
                // Update previous and current selection
                val previousPosition = addresses.indexOfFirst { it.addressId == previousSelected }
                if (previousPosition != -1) {
                    notifyItemChanged(previousPosition)
                }
                notifyItemChanged(position)
                
                onAddressSelected(address)
            }
        }
    }

    override fun getItemCount(): Int = addresses.size

    fun updateAddresses(newAddresses: List<Address>) {
        addresses.clear()
        addresses.addAll(newAddresses)
        
        // Auto-select default address if none selected
        if (selectedAddressId == -1) {
            val defaultAddress = addresses.find { it.isDefault == 1 }
            if (defaultAddress != null) {
                selectedAddressId = defaultAddress.addressId
                onAddressSelected(defaultAddress)
            }
        }
        
        notifyDataSetChanged()
    }

    fun getSelectedAddress(): Address? {
        return addresses.find { it.addressId == selectedAddressId }
    }
}
