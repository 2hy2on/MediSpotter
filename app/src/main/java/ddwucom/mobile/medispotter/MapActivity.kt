package ddwucom.mobile.medispotter

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SearchView  // Add this import statement
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import ddwucom.mobile.medispotter.data.Hospital
import ddwucom.mobile.medispotter.data.HospitalDao
import ddwucom.mobile.medispotter.data.HospitalDatabase
import ddwucom.mobile.medispotter.databinding.ActivityMapBinding
import ddwucom.mobile.medispotter.network.hospitalDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.util.Locale


class MapActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {

    private lateinit var googleMap : GoogleMap
    private lateinit var mapBinding : ActivityMapBinding
    var centerMarker : Marker? = null

    var searchRes: List<Hospital>? =null
    private lateinit var fusedLocationClient : FusedLocationProviderClient
    private lateinit var geocoder : Geocoder
    private lateinit var currentLoc : Location

    var selectedType: String? = null
    var selectedOption : String? = null
    var selectedOpen: String? = null

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        mapBinding =  ActivityMapBinding.inflate(layoutInflater)
        setContentView(mapBinding.root)



        mapBinding.searchView.isSubmitButtonEnabled = true
        val queryTextListener = object : SearchView.OnQueryTextListener {
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
        }
        mapBinding.searchView.setOnQueryTextListener(queryTextListener)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this@MapActivity)

        geocoder = Geocoder(this, Locale.getDefault())

        val mapFragment: SupportMapFragment
                = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment

        mapFragment.getMapAsync(mapReadyCallback)

        mapBinding.button2.setOnClickListener {
            checkPermissions ()
        }


        mapBinding.button.setOnClickListener {
            startLocUpdates()
        }
//        showData("Geocoder isEnabled: ${Geocoder.isPresent()}")

        //spinner
        val typeSpinner = mapBinding.type
        val openSpinner = mapBinding.open
        val optionSpinner = mapBinding.option
        // Create arrays for spinner items
        val typeItems = arrayOf("한의원", "치과의원", "의원", "요양병원", "보건소", "한방병원", "내과", "산부인과")
        val openItems = arrayOf("전체", "영업중")
        val optionItems = arrayOf("지역검색", "이름검색")

        // Create ArrayAdapter for each Spinner
        val typeAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, typeItems)
        val openAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, openItems)
        val optionAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, optionItems)

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

    }

    val mapReadyCallback = object: OnMapReadyCallback {
        override fun onMapReady(map: GoogleMap) {
            googleMap = map

            googleMap.setOnInfoWindowClickListener { marker ->
                val hospitalId = marker.tag as? Int
                if (hospitalId != null) {
                    val intent = Intent(this@MapActivity, HospitalDetailActivity::class.java)
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


    @RequiresApi(Build.VERSION_CODES.N)
    fun checkPermissions () {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
            && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            Log.d("dd","Permissions are already granted")  // textView에 출력
        } else {
            locationPermissionRequest.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ))
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
    val locCallback : LocationCallback = object: LocationCallback(){
        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        override fun onLocationResult(locResult: LocationResult) {
            currentLoc = locResult.locations[0]
            Log.d("dd","위도: ${currentLoc.latitude}, 경도: ${currentLoc.longitude}")
            var targetLoc = LatLng(currentLoc.latitude, currentLoc.longitude)
            geocoder.getFromLocation(currentLoc.latitude, currentLoc.longitude,5){
                    address->
                CoroutineScope(Dispatchers.Main).launch {
                    Log.d("dd",address.get(0).getAddressLine(0).toString())
                }
            }
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(targetLoc,17F))
            centerMarker?.remove()
            addMarker(targetLoc)
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

    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if(location != null){
                Log.d("dd",location.toString())
            }
        }
    }

    fun addMarker(targetLoc: LatLng) {  // LatLng(37.606320, 127.041808)
        val markerOptions : MarkerOptions = MarkerOptions()
        markerOptions.position(targetLoc)
            .title("제목")
            .snippet("말풍선")
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

    override fun onNothingSelected(parent: AdapterView<*>?) {
        TODO("Not yet implemented")
    }

    private fun performSearch(query: String?) {

        if (!query.isNullOrBlank()) {
            if(selectedOption=="지역검색"){
                Log.d("MapActivity", "Selected Type: $selectedType")
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

        }
    }

}




