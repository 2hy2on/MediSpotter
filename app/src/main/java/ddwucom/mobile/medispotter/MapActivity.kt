package ddwucom.mobile.medispotter

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
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
import ddwucom.mobile.medispotter.databinding.ActivityMapBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale

class MapActivity : AppCompatActivity() {
    val mapBinding by lazy {
        ActivityMapBinding.inflate(layoutInflater)
    }
    private lateinit var googleMap : GoogleMap
    var centerMarker : Marker? = null

    private lateinit var fusedLocationClient : FusedLocationProviderClient
    private lateinit var geocoder : Geocoder
    private lateinit var currentLoc : Location

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(mapBinding.root)


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
    }

    val mapReadyCallback = object: OnMapReadyCallback {
        override fun onMapReady(map: GoogleMap) {
            googleMap = map
        }
    }

    override fun onPause() {
        super.onPause()
        fusedLocationClient.removeLocationUpdates(locCallback)
    }


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

}