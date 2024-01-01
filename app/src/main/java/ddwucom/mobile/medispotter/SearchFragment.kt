package ddwucom.mobile.medispotter

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SearchView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.fragment.app.Fragment
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import ddwucom.mobile.medispotter.data.Hospital
import ddwucom.mobile.medispotter.databinding.FragmentSearchBinding
import ddwucom.mobile.medispotter.network.hospitalDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.util.Date
import java.util.Locale

class SearchFragment : Fragment(), AdapterView.OnItemSelectedListener {

    private lateinit var googleMap: GoogleMap
    private lateinit var mapBinding: FragmentSearchBinding
    private var centerMarker: Marker? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var geocoder: Geocoder
    private lateinit var currentLoc: Location
    private var searchRes: List<Hospital>? = null
    private var selectedType: String? = null
    private var selectedOption: String? = null
    private var selectedOpen: String? = null

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mapBinding = FragmentSearchBinding.inflate(inflater, container, false)
        return mapBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        geocoder = Geocoder(requireContext(), Locale.getDefault())

        val mapFragment: SupportMapFragment =
            childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment


        mapFragment.getMapAsync(mapReadyCallback)

        checkPermissions ()



        mapBinding.button.setOnClickListener {
            startLocUpdates()
        }
        mapBinding.stopBtn.setOnClickListener {
            fusedLocationClient.removeLocationUpdates(locCallback)
        }

        //spinner
        val typeSpinner = mapBinding.type
        val openSpinner = mapBinding.open
        val optionSpinner = mapBinding.option
        // Create arrays for spinner items
        val typeItems = arrayOf("한의원", "치과의원", "의원", "요양병원", "보건소", "한방병원", "내과", "산부인과")
        val openItems = arrayOf("전체", "진료중")
        val optionItems = arrayOf("지역검색", "이름검색")

        val typeAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, typeItems)
        val openAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, openItems)
        val optionAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, optionItems)


        // Set dropdown layout style
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        openAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        optionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        // Set ArrayAdapter to Spinners
        typeSpinner.adapter = typeAdapter
        openSpinner.adapter = openAdapter
        optionSpinner.adapter = optionAdapter

        typeSpinner.onItemSelectedListener = this
        openSpinner.onItemSelectedListener = this
        optionSpinner.onItemSelectedListener = this

        val searchView = mapBinding.searchView

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrBlank()) {
                    performSearch(query)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // Handle query text change
                // You can use this for live search suggestions, etc.
                return true
            }
        })
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        TODO("Not yet implemented")
    }
    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if(location != null){
                Log.d("dd",location.toString())
            }
        }
    }

    val mapReadyCallback = object: OnMapReadyCallback {
        override fun onMapReady(map: GoogleMap) {
            googleMap = map

            googleMap.setOnInfoWindowClickListener { marker ->
                val hospitalId = marker.tag as? Int
                if (hospitalId != null) {
                    val intent = Intent(requireContext(), HospitalDetailActivity::class.java)
                    intent.putExtra("hospitalId", hospitalId)
                    startActivity(intent)
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        fusedLocationClient.removeLocationUpdates(locCallback)
    }

    private fun checkPermissions() {
        if (checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            && checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            Log.d("dd", "Permissions are already granted")  // textView에 출력
        } else {
            locationPermissionRequest.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    /*registerForActivityResult 는 startActivityForResult() 대체*/
    @RequiresApi(Build.VERSION_CODES.N)
    val locationPermissionRequest
            = registerForActivityResult( ActivityResultContracts.RequestMultiplePermissions() ) {
            permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                Log.d("dd","FINE_LOCATION is granted")
            }
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                Log.d("dd","COARSE_LOCATION is granted")
            }
            else -> {
                Log.d("dd","Location permissions are required")
            }
        }
    }


    //위치정보가 왔을 때 결과받음
    val locCallback: LocationCallback = object : LocationCallback() {
        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        override fun onLocationResult(locResult: LocationResult) {
            if (locResult.locations.isNotEmpty()) {
                currentLoc = locResult.locations[0]
                Log.d("dd", "위도: ${currentLoc.latitude}, 경도: ${currentLoc.longitude}")
                var targetLoc = LatLng(currentLoc.latitude, currentLoc.longitude)
                geocoder.getFromLocation(currentLoc.latitude, currentLoc.longitude, 5) { address ->
                    CoroutineScope(Dispatchers.Main).launch {
                        Log.d("dd", address.get(0).getAddressLine(0).toString())
                    }
                }
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(targetLoc, 17F))
                centerMarker?.remove()
                addMarker(targetLoc)
            } else {
                Log.e("dd", "No location result")
            }
        }
    }

    val locRequest : LocationRequest = LocationRequest.Builder(5000)
        .setMinUpdateIntervalMillis(10000)
        .setPriority(Priority.PRIORITY_BALANCED_POWER_ACCURACY)
        .build()

    @SuppressLint("MissingPermission")
    private fun startLocUpdates() {
        fusedLocationClient.requestLocationUpdates(
            locRequest,
            locCallback,
            Looper.getMainLooper()
        )

    }


    fun addMarker(targetLoc: LatLng) {  // LatLng(37.606320, 127.041808)
        val markerOptions : MarkerOptions = MarkerOptions()
        markerOptions.position(targetLoc)
            .title("내위치")
            . icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))

        centerMarker = googleMap.addMarker(markerOptions)
        centerMarker?.showInfoWindow()
        centerMarker?.tag = "database_id"


    }

    fun addHosMarker(hospitals: List<Hospital>) {  // LatLng(37.606320, 127.041808)
        var hospitalLoc: LatLng?= null

        for (hospital in hospitals) {

            Log.d("MapActivity", " ${hospital.longitude!!.toDouble()}")
            hospitalLoc = LatLng(hospital.latitude!!.toDouble(), hospital.longitude!!.toDouble())

            val markerOptions: MarkerOptions = MarkerOptions()
            if (hospitalLoc != null) {
                markerOptions.position(hospitalLoc)
                    .title(hospital.name)  // Use hospital name as the marker title
                    .snippet(hospital.dutyTel1)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
            }


            val marker = googleMap.addMarker(markerOptions)
            marker?.showInfoWindow()

            marker?.tag = hospital._id
        }
        hospitalLoc?.let { CameraUpdateFactory.newLatLngZoom(it,17F) }
            ?.let { googleMap.animateCamera(it) }

    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        when (parent?.id) {
            R.id.type -> {
                // Handle selection for typeSpinner
                selectedType = parent.getItemAtPosition(position).toString()
                Log.d("MapActivity", "Selected Type: $selectedType")
            }
            R.id.open -> {
                // Handle selection for openSpinner
                selectedOpen = parent.getItemAtPosition(position).toString()
                Log.d("MapActivity", "Selected Open Status: $selectedOpen")
            }
            R.id.option->{
                selectedOption = parent.getItemAtPosition(position).toString()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun performSearch(query: String?) {

        googleMap.clear()

        val currentTime = Date()
        val dateFormat = SimpleDateFormat("HHmm", Locale.getDefault())
        val formattedTime: String = dateFormat.format(currentTime)

        val currentDate = LocalDate.now()

        val dayOfWeek = currentDate.dayOfWeek
        val (startColumn, endColumn) = mapDayOfWeekToColumns(dayOfWeek) // Corrected assignment


        if (!query.isNullOrBlank()) {
            if(selectedOption=="지역검색"){
                Log.d("MapActivity", "Selected Type: $selectedType")
                if(selectedOpen != "진료중") {
                    CoroutineScope(Dispatchers.Main).launch {
                        try {
                            searchRes = hospitalDao.getHospitalByAddrType(query, selectedType!!)
                            searchRes?.let {
                                Log.d("NM", it.size.toString())
                                addHosMarker(it)
                            }
                        } catch (e: Exception) {
                            // Handle exceptions if necessary
                            e.printStackTrace()
                        }
                    }
                }
                else{
                    CoroutineScope(Dispatchers.Main).launch {
                        try {
                            Log.d("NM", formattedTime)
                            when (startColumn) {

                                "dutyTime1s" -> {
                                    searchRes = hospitalDao.getHospitalAddrMonOpen(query, selectedType!!, formattedTime)
                                }
                                "dutyTime2s" -> {
                                    searchRes = hospitalDao.getHospitalAddrTueOpen(query, selectedType!!, formattedTime)
                                }
                                "dutyTime3s" -> {
                                    searchRes = hospitalDao.getHospitalAddrWedOpen(query, selectedType!!, formattedTime)
                                }
                                "dutyTime4s" -> {
                                    searchRes = hospitalDao.getHospitalAddrThurOpen(query, selectedType!!, formattedTime)
                                }
                                "dutyTime5s" -> {
                                    searchRes = hospitalDao.getHospitalAddrFriOpen(query, selectedType!!, formattedTime)
                                }
                                "dutyTime6s" -> {
                                    searchRes = hospitalDao.getHospitalAddrSatOpen(query, selectedType!!, formattedTime)
                                }
                                "dutyTime7s" -> {
                                    searchRes = hospitalDao.getHospitalAddrSunOpen(query, selectedType!!, formattedTime)
                                }
                            }

                            searchRes?.let {
                                Log.d("NM", it.size.toString())
                                addHosMarker(it)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
            else{
                if(selectedOpen !="진료중") {
                    CoroutineScope(Dispatchers.Main).launch {
                        try {
                            searchRes = hospitalDao.getHospitalByNameType(query, selectedType!!)
                            searchRes?.let {
                                Log.d("NM", it.size.toString())
                                addHosMarker(it)
                            }
                        } catch (e: Exception) {
                            // Handle exceptions if necessary
                            e.printStackTrace()
                        }
                    }
                }
                else{
                    CoroutineScope(Dispatchers.Main).launch {
                        try {
                            when (startColumn) {
                                "dutyTime1s" -> {
                                    searchRes = hospitalDao.getHospitalNameMonOpen(query, selectedType!!, formattedTime)
                                }
                                "dutyTime2s" -> {
                                    searchRes = hospitalDao.getHospitalNameTueOpen(query, selectedType!!, formattedTime)
                                }
                                "dutyTime3s" -> {
                                    searchRes = hospitalDao.getHospitalNameWedOpen(query, selectedType!!, formattedTime)
                                }
                                "dutyTime4s" -> {
                                    searchRes = hospitalDao.getHospitalNameThurOpen(query, selectedType!!, formattedTime)
                                }
                                "dutyTime5s" -> {
                                    searchRes = hospitalDao.getHospitalNameFriOpen(query, selectedType!!, formattedTime)
                                }
                                "dutyTime6s" -> {
                                    searchRes = hospitalDao.getHospitalNameSatOpen(query, selectedType!!, formattedTime)
                                }
                                "dutyTime7s" -> {
                                    searchRes = hospitalDao.getHospitalNameSunOpen(query, selectedType!!, formattedTime)
                                }
                            }

                            searchRes?.let {
                                Log.d("NM", it.size.toString())
                                addHosMarker(it)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }

        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun mapDayOfWeekToColumns(dayOfWeek: DayOfWeek): Pair<String, String> {
        return when (dayOfWeek) {
            DayOfWeek.MONDAY -> Pair("dutyTime1s", "dutyTime1c")
            DayOfWeek.TUESDAY -> Pair("dutyTime2s", "dutyTime2c")  // Add the correct column names for Tuesday
            DayOfWeek.WEDNESDAY -> Pair("dutyTime3s", "dutyTime3c")
            DayOfWeek.THURSDAY -> Pair("dutyTime4s", "dutyTime4c")
            DayOfWeek.FRIDAY -> Pair("dutyTime5s", "dutyTime5c")
            DayOfWeek.SATURDAY -> Pair("dutyTime6s", "dutyTime6c")
            DayOfWeek.SUNDAY -> Pair("dutyTime7s", "dutyTime7c")
        }
    }

}

