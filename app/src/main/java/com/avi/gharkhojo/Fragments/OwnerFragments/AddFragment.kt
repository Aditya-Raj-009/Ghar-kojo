package com.avi.gharkhojo.Fragments.OwnerFragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.avi.gharkhojo.R

class AddFragment : Fragment() {

    private lateinit var ownerNameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var tenantServedEditText: EditText
    private lateinit var propertyTypeSpinner: Spinner
    private lateinit var preferredTenantsSpinner: Spinner
    private lateinit var phoneNumberEditText: EditText
    private lateinit var nextButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add, container, false)
        initializeViews(view)
        setupSpinners()
        setupNextButton()
        loadData()
        return view
    }

    private fun initializeViews(view: View) {
        ownerNameEditText = view.findViewById(R.id.ownerNameEditText)
        emailEditText = view.findViewById(R.id.emailEditText)
        tenantServedEditText = view.findViewById(R.id.tenantServedEditText)
        propertyTypeSpinner = view.findViewById(R.id.propertyTypeSpinner)
        preferredTenantsSpinner = view.findViewById(R.id.preferredTenantsSpinner)
        phoneNumberEditText = view.findViewById(R.id.phoneNumberEditText)
        nextButton = view.findViewById(R.id.nextButton)
    }

    private fun setupSpinners() {
        val propertyTypes = listOf("House", "Apartment", "Flat", "Dormitory", "Luxury", "Commercial")
        val propertyTypeAdapter = AccessibleSpinnerAdapter(requireContext(), R.layout.custom_spinner_item, propertyTypes)
        propertyTypeAdapter.setDropDownViewResource(R.layout.custom_spinner_dropdown_item)
        propertyTypeSpinner.adapter = propertyTypeAdapter

        val preferredTenants = listOf("Family", "Only Girls", "Only Boys", "Bachelors", "Any")
        val preferredTenantsAdapter = AccessibleSpinnerAdapter(requireContext(), R.layout.custom_spinner_item, preferredTenants)
        preferredTenantsAdapter.setDropDownViewResource(R.layout.custom_spinner_dropdown_item)
        preferredTenantsSpinner.adapter = preferredTenantsAdapter
    }

    private fun setupNextButton() {
        nextButton.setOnClickListener {
            if (validateInputs()) {
                saveData()
                findNavController().navigate(R.id.action_addFragment_to_propertyDetailsFragment)
            }
        }
    }

    private fun validateInputs(): Boolean {
        var isValid = true

        if (ownerNameEditText.text.isNullOrBlank()) {
            ownerNameEditText.error = "Owner name is required"
            ownerNameEditText.announceForAccessibility("Error: Owner name is required")
            isValid = false
        }

        if (emailEditText.text.isNullOrBlank()) {
            emailEditText.error = "Email is required"
            emailEditText.announceForAccessibility("Error: Email is required")
            isValid = false
        }

        if (tenantServedEditText.text.isNullOrBlank()) {
            tenantServedEditText.error = "Tenant served is required"
            tenantServedEditText.announceForAccessibility("Error: Tenant served is required")
            isValid = false
        }

        if (phoneNumberEditText.text.isNullOrBlank()) {
            phoneNumberEditText.error = "Phone number is required"
            phoneNumberEditText.announceForAccessibility("Error: Phone number is required")
            isValid = false
        }

        return isValid
    }

    private fun saveData() {
        val sharedPref = activity?.getSharedPreferences("OwnerData", Context.MODE_PRIVATE) ?: return
        with(sharedPref.edit()) {
            putString("ownerName", ownerNameEditText.text.toString())
            putString("email", emailEditText.text.toString())
            putString("tenantServed", tenantServedEditText.text.toString())
            putString("propertyType", propertyTypeSpinner.selectedItem.toString())
            putString("preferredTenants", preferredTenantsSpinner.selectedItem.toString())
            putString("phoneNumber", phoneNumberEditText.text.toString())
            apply()
        }
    }

    private fun loadData() {
        val sharedPref = activity?.getSharedPreferences("OwnerData", Context.MODE_PRIVATE) ?: return
        ownerNameEditText.setText(sharedPref.getString("ownerName", ""))
        emailEditText.setText(sharedPref.getString("email", ""))
        tenantServedEditText.setText(sharedPref.getString("tenantServed", ""))
        phoneNumberEditText.setText(sharedPref.getString("phoneNumber", ""))

        val propertyType = sharedPref.getString("propertyType", "")
        val propertyTypePosition = (propertyTypeSpinner.adapter as ArrayAdapter<String>).getPosition(propertyType)
        if (propertyTypePosition != -1) {
            propertyTypeSpinner.setSelection(propertyTypePosition)
        }

        val preferredTenants = sharedPref.getString("preferredTenants", "")
        val preferredTenantsPosition = (preferredTenantsSpinner.adapter as ArrayAdapter<String>).getPosition(preferredTenants)
        if (preferredTenantsPosition != -1) {
            preferredTenantsSpinner.setSelection(preferredTenantsPosition)
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = AddFragment()
    }
}

class AccessibleSpinnerAdapter(context: Context, resource: Int, objects: List<String>) :
    ArrayAdapter<String>(context, resource, objects) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getView(position, convertView, parent)
        if (view is TextView) {
            view.contentDescription = "Selected property type: ${getItem(position)}"
        }
        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getDropDownView(position, convertView, parent)
        view.setBackgroundResource(android.R.color.black)
        (view as? TextView)?.setTextColor(context.resources.getColor(android.R.color.white, null))
        return view
    }
}