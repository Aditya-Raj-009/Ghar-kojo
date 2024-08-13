package com.avi.gharkhojo.Fragments.OwnerFragments

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.avi.gharkhojo.R
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class RentAndLocationFragment : Fragment() {
    private lateinit var checkBoxParkingIncluded: CheckBox
    private lateinit var textInputLayoutParkingCharges: TextInputLayout
    private lateinit var btnLiveLocation: Button
    private lateinit var btnTurnOnLocation: Button
    private lateinit var btnNext: Button
    private lateinit var textViewLongitude: TextView
    private lateinit var textViewLatitude: TextView
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var imageViewDeletePriceDetails: ImageView


    companion object {
        private const val REQUEST_CHECK_SETTINGS = 1002
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_rent_and_location, container, false)

        initializeViews(view)
        setupLocationServices()
        setupAnimations(view)
        setupListeners()
        loadSavedData()

        return view
    }

    private fun initializeViews(view: View) {
        checkBoxParkingIncluded = view.findViewById(R.id.checkBoxParkingIncluded)
        textInputLayoutParkingCharges = view.findViewById(R.id.textInputLayoutParkingCharges)
        btnLiveLocation = view.findViewById(R.id.btnLiveLocation)
        btnTurnOnLocation = view.findViewById(R.id.btnTurnOnLocation)
        btnNext = view.findViewById(R.id.btnNext)
        textViewLongitude = view.findViewById(R.id.textViewLongitude)
        textViewLatitude = view.findViewById(R.id.textViewLatitude)
        imageViewDeletePriceDetails = view.findViewById(R.id.imageViewDeletePriceDetails)
        textViewLongitude.visibility = View.GONE
        textViewLatitude.visibility = View.GONE
    }

    private fun setupLocationServices() {
        sharedPreferences = requireActivity().getSharedPreferences("RentLocationPrefs", Context.MODE_PRIVATE)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
    }
    private fun setupAnimations(view: View) {
        val cardViews = listOf<CardView>(
            view.findViewById(R.id.cardViewPriceDetails),
            view.findViewById(R.id.cardViewLocationDetails),
            view.findViewById(R.id.cardViewPropertyDescription)
        )

        val animatorSet = AnimatorSet()
        val animators = cardViews.mapIndexed { index, cardView ->
            ObjectAnimator.ofFloat(cardView, "translationY", 1000f, 0f).apply {
                duration = 500
                startDelay = index * 100L
            }
        }

        animatorSet.playTogether(*animators.toTypedArray())
        animatorSet.start()
    }
    private fun setupListeners() {
        checkBoxParkingIncluded.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                textInputLayoutParkingCharges.visibility = View.GONE
            } else {
                textInputLayoutParkingCharges.visibility = View.VISIBLE
                textInputLayoutParkingCharges.alpha = 0f
                textInputLayoutParkingCharges.animate().alpha(1f).duration = 300
            }
        }

        view?.findViewById<View>(R.id.btnNext)?.setOnClickListener {
            it.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(100)
                .withEndAction {
                    it.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(100)
                        .start()
                }
                .start()
        }

        setupTextInputAnimations()

        btnTurnOnLocation.setOnClickListener {
            checkLocationPermission()
        }

        btnLiveLocation.setOnClickListener {
            btnLiveLocation.isEnabled = false
            lifecycleScope.launch {
                getLocation()
                delayButton(btnLiveLocation, 3000)
            }
        }

        imageViewDeletePriceDetails.setOnClickListener {
            clearStoredData()
        }
        btnNext.setOnClickListener {
            findNavController().navigate(R.id.action_rentAndLocationFragment_to_roomPhotosFragment)
        }
    }
    private fun setupTextInputAnimations() {
        val autoCompleteTextViews = listOf(
            R.id.autoCompleteTextViewRent, R.id.autoCompleteTextViewBrokerage,
            R.id.autoCompleteTextViewDeposit, R.id.autoCompleteTextViewPincode,
            R.id.autoCompleteTextViewLandmark, R.id.autoCompleteTextViewHouseNumber,
            R.id.autoCompleteTextViewArea, R.id.autoCompleteTextViewColony,
            R.id.autoCompleteTextViewCity
        )

        autoCompleteTextViews.forEach { autoCompleteTextViewId ->
            view?.findViewById<AutoCompleteTextView>(autoCompleteTextViewId)?.setOnFocusChangeListener { v, hasFocus ->
                v.animate().scaleX(if (hasFocus) 1.05f else 1f).scaleY(if (hasFocus) 1.05f else 1f).setDuration(200).start()
            }
        }

        view?.findViewById<TextInputLayout>(R.id.textInputLayoutPropertyDescription)
            ?.editText?.setOnFocusChangeListener { v, hasFocus ->
                v.animate().scaleX(if (hasFocus) 1.05f else 1f).scaleY(if (hasFocus) 1.05f else 1f).setDuration(200).start()
            }
    }


    private fun clearStoredData() {
        // Clear SharedPreferences
        sharedPreferences.edit().clear().apply()

        // Clear input fields
        view?.findViewById<AutoCompleteTextView>(R.id.autoCompleteTextViewRent)?.text?.clear()
        view?.findViewById<AutoCompleteTextView>(R.id.autoCompleteTextViewParkingCharges)?.text?.clear()
        view?.findViewById<AutoCompleteTextView>(R.id.autoCompleteTextViewBrokerage)?.text?.clear()
        view?.findViewById<AutoCompleteTextView>(R.id.autoCompleteTextViewDeposit)?.text?.clear()
        view?.findViewById<AutoCompleteTextView>(R.id.autoCompleteTextViewPincode)?.text?.clear()
        view?.findViewById<AutoCompleteTextView>(R.id.autoCompleteTextViewLandmark)?.text?.clear()
        view?.findViewById<AutoCompleteTextView>(R.id.autoCompleteTextViewHouseNumber)?.text?.clear()
        view?.findViewById<AutoCompleteTextView>(R.id.autoCompleteTextViewArea)?.text?.clear()
        view?.findViewById<AutoCompleteTextView>(R.id.autoCompleteTextViewColony)?.text?.clear()
        view?.findViewById<AutoCompleteTextView>(R.id.autoCompleteTextViewCity)?.text?.clear()
        view?.findViewById<TextInputEditText>(R.id.editTextPropertyDescription)?.text?.clear()

        // Clear location data
        textViewLongitude.text = "Longitude: "
        textViewLatitude.text = "Latitude: "
        textViewLongitude.visibility = View.GONE
        textViewLatitude.visibility = View.GONE

        Toast.makeText(context, "All data has been cleared", Toast.LENGTH_SHORT).show()
    }

    private suspend fun getLocation() {
        if (!isLocationEnabled()) {
            promptUserToEnableLocationSettings()
            return
        }

        when {
            hasPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) -> fetchLocation(Priority.PRIORITY_HIGH_ACCURACY)
            hasPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) -> fetchLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY)
            else -> requestPermissions(
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    private suspend fun fetchLocation(priority: Int) {
        try {
            // Check for permission before proceeding
            if (!hasPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) &&
                !hasPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION)) {
                requestPermissions(
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION),
                    LOCATION_PERMISSION_REQUEST_CODE
                )
                return
            }

            val locationResult = fusedLocationClient.getCurrentLocation(priority, null).await()

            locationResult?.let {
                updateUIWithLocation(it.latitude, it.longitude)
                saveLocation(it.latitude, it.longitude)
            } ?: run {
                Toast.makeText(context, "Failed to obtain location", Toast.LENGTH_LONG).show()
            }
        } catch (e: SecurityException) {
            Toast.makeText(context, "Location permission denied", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Error getting location: ${e.message}", Toast.LENGTH_LONG).show()
        } finally {
            btnLiveLocation.isEnabled = true
        }
    }


    private fun updateUIWithLocation(latitude: Double, longitude: Double) {
        textViewLongitude.text = "Longitude: $longitude"
        textViewLatitude.text = "Latitude: $latitude"
        textViewLongitude.visibility = View.VISIBLE
        textViewLatitude.visibility = View.VISIBLE
        Toast.makeText(context, "Location obtained successfully", Toast.LENGTH_SHORT).show()
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun promptUserToEnableLocationSettings() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000).build()
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val settingsClient = LocationServices.getSettingsClient(requireActivity())
        val task = settingsClient.checkLocationSettings(builder.build())

        task.addOnSuccessListener {
            // Location settings are satisfied, nothing to do here.
        }.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                try {
                    exception.startResolutionForResult(requireActivity(), REQUEST_CHECK_SETTINGS)
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CHECK_SETTINGS && resultCode == RESULT_OK) {
            lifecycleScope.launch {
                getLocation()
                delayButton(btnLiveLocation, 3000)
            }
        } else {
            Toast.makeText(context, "Please enable location services", Toast.LENGTH_LONG).show()
            btnLiveLocation.isEnabled = true
        }
    }

    private fun saveLocation(latitude: Double, longitude: Double) {
        sharedPreferences.edit().apply {
            putFloat("latitude", latitude.toFloat())
            putFloat("longitude", longitude.toFloat())
            apply()
        }
    }

    private fun loadSavedData() {
        sharedPreferences.apply {
            val latitude = getFloat("latitude", 0f).toDouble()
            val longitude = getFloat("longitude", 0f).toDouble()

            if (latitude != 0.0 && longitude != 0.0) {
                updateUIWithLocation(latitude, longitude)
            }
        }
    }

    private fun checkLocationPermission() {
        when {
            hasPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) -> {
                Toast.makeText(context, "Precise location permission is already enabled", Toast.LENGTH_SHORT).show()
            }
            hasPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) -> {
                Toast.makeText(context, "Approximate location permission is already enabled", Toast.LENGTH_SHORT).show()
            }
            else -> {
                requestPermissions(
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION),
                    LOCATION_PERMISSION_REQUEST_CODE
                )
            }
        }
    }

    private fun hasPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED
    }

    private suspend fun delayButton(button: Button, delay: Long) {
        button.isEnabled = false
        kotlinx.coroutines.delay(delay)
        button.isEnabled = true
    }
}
