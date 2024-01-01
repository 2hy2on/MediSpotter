package ddwucom.mobile.medispotter

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ContentValues
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.Fragment
import ddwucom.mobile.medispotter.databinding.ActivityMainBinding
import ddwucom.mobile.medispotter.network.NetworkManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    lateinit var mainBinding : ActivityMainBinding


    private val networkDao by lazy { NetworkManager(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mainBinding.root)
        createNotificationChannel()
        CoroutineScope(Dispatchers.Main).launch {
             withContext(Dispatchers.IO) {
                try {
                    networkDao.downloadXml()
                } catch (e: Exception) {
                    Log.e(ContentValues.TAG, "Error downloading data: ${e.message}")
                    null
                }
            }
        }
        replaceFragment(SearchFragment())

        mainBinding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.search_bt -> {
                    replaceFragment(SearchFragment())
                    true // Return true to indicate that the item is selected
                }
                R.id.favorite_btn -> {
                    replaceFragment(FavoriteFragment())
                    true // Return true to indicate that the item is selected
                }
                R.id.review_btn -> {
                    replaceFragment(ReviewFragment())
                    true // Return true to indicate that the item is selected
                }
                else -> false // Return false for unrecognized items
            }
        }



    }

    private fun replaceFragment(fragment: Fragment){
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_layout, fragment)
        fragmentTransaction.commit()
    }
    private fun createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Notification Channel 의 생성
            val channelID = resources.getString(R.string.channel_id)
            val name = "Test Channel"
            val descriptionText = "Test Channel Message"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val mChannel = NotificationChannel(channelID, name, importance)
            mChannel.description = descriptionText

            // Channel 을 시스템에 등록, 등록 후에는 중요도 변경 불가
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(mChannel)

//  알림 허용 전에는 false 허용하면 True 뜸 - 앱 실행도중 승인 받음 are뭐시기가 승인여부 확인하는 코드임
            Toast.makeText(applicationContext, "${notificationManager.areNotificationsEnabled()}", Toast.LENGTH_SHORT).show()
        }
    }
}
