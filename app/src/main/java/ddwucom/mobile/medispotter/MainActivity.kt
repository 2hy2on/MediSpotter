package ddwucom.mobile.medispotter

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.room.Room
//import androidx.room.Room
import ddwu.com.mobile.openapitest.network.NetworkManager
import ddwucom.mobile.medispotter.data.HospitalDao
import ddwucom.mobile.medispotter.data.HospitalDatabase
//import ddwucom.mobile.medispotter.data.Hospital
//import ddwucom.mobile.medispotter.data.HospitalDao
//import ddwucom.mobile.medispotter.data.HospitalDatabase
import ddwucom.mobile.medispotter.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException

class MainActivity : AppCompatActivity() {
    lateinit var mainBinding : ActivityMainBinding


    private val networkDao by lazy { NetworkManager(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mainBinding.root)

        CoroutineScope(Dispatchers.Main).launch {
            val result = withContext(Dispatchers.IO) {
                try {
                    networkDao.downloadXml()
                } catch (e: Exception) {
                    Log.e(TAG, "Error downloading data: ${e.message}")
                    null
                }
            }
        }
        mainBinding.toMapBtn.setOnClickListener {
            val intent =  Intent(this, MapActivity::class.java)
            startActivity(intent)
        }

        mainBinding.toMomoBtn.setOnClickListener {
            val intent =  Intent(this, MemoActivity::class.java)
            startActivity(intent)
        }
    }
}