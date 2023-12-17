package ddwucom.mobile.medispotter

import android.content.ContentValues.TAG
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import ddwu.com.mobile.openapitest.network.NetworkManager
import ddwucom.mobile.medispotter.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException

class MainActivity : AppCompatActivity() {
    lateinit var mainBinding : ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mainBinding.root)

        val networkDao = NetworkManager(this)

        mainBinding.btn.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch { 
                val def = async(Dispatchers.IO) {
                    var txt: String? = null
                    
                    try {
                        txt = networkDao.downloadXml("서울특별시", "양천구").toString()

                    }catch (e: IOException) {
                        Log.d(TAG, e.message?: "null")
                        null
                    } catch (e: XmlPullParserException) {
                        Log.d(TAG, e.message?: "null")
                        null
                    }
                    txt
                }
                mainBinding.text.text = def.await()
            }

        }
    }
}