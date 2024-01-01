package ddwucom.mobile.medispotter.network

import android.content.Context
import android.util.Log
import androidx.room.Room
import ddwucom.mobile.medispotter.R
import ddwucom.mobile.medispotter.data.Hospital
import ddwucom.mobile.medispotter.data.HospitalDao
import ddwucom.mobile.medispotter.data.HospitalDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.InputStream
import java.net.URL
import java.net.URLEncoder
import javax.net.ssl.HttpsURLConnection
import kotlin.jvm.Throws

lateinit var db : HospitalDatabase
lateinit var hospitalDao: HospitalDao
class NetworkManager(val context: Context) {
    private val TAG = "NetworkManager"

    //
    val openApiUrl by lazy {
        context.resources.getString(R.string.hospital_url)
    }

    init {
        db = Room.databaseBuilder(context.applicationContext,
            HospitalDatabase::class.java, "hospital_db")
            .build()
        /*FoodDatabase 생성*/
        hospitalDao = db.hospitalDao()


    }
    private suspend fun saveHospitalsToDatabase(hospitals: List<Hospital>) {
        withContext(Dispatchers.IO) {
            hospitalDao.insertAll(hospitals)
        }
    }
    @Throws(IOException::class)
    suspend fun downloadXml() : List<Hospital>? {    val urlBuilder = StringBuilder(openApiUrl)

        val hospitals = mutableListOf<Hospital>()

        val chunkSize = 8
        for (chunk in 0 until 2) { // 16 / 8 = 2 chunks
            for (pageNo in chunk * chunkSize + 1..(chunk + 1) * chunkSize) {
                val urlBuilder = StringBuilder(openApiUrl)
                urlBuilder.append("?")
                    .append(URLEncoder.encode("serviceKey", "UTF-8"))
                    .append("=VgnAmvRIZCJfNOvf4sf9%2BXH53u6BdwKRdv9bixebAuK1tCRqJlVT%2B0Do76KmIUP3uz1yAmjjRZm6DPYa9iM%2FeA%3D%3D")
                    .append("&")
                    .append(URLEncoder.encode("pageNo", "UTF-8"))
                    .append("=")
                    .append(pageNo)
                    .append("&")
                    .append(URLEncoder.encode("numOfRows", "UTF-8"))
                    .append("=")
                    .append(5000)

                val inputStream = downloadUrl(urlBuilder)
                inputStream?.let {
                    val parser = HospitalParser()
                    val hospitalsPerPage = parser.parse(it)
                    hospitals.addAll(hospitalsPerPage)
                    Log.d("Page $pageNo parsed", "Total Hospitals: ${hospitals.size}")
                }

                // Close the InputStream after reading its content
                inputStream?.close()
            }

            // Save hospitals to the Room database after each chunk
            saveHospitalsToDatabase(hospitals)

            // Clear the list for the next chunk
            hospitals.clear()
        }

        return hospitals.takeIf { it.isNotEmpty() }
    }


    @Throws(IOException::class)
    private suspend fun downloadUrl(urlString: StringBuilder) : InputStream? {
        return withContext(Dispatchers.IO) {
            val url = URL(urlString.toString())
            Log.d("DSd", url.toString())
            (url.openConnection() as? HttpsURLConnection)?.run {
                readTimeout = 10000
                connectTimeout = 10000
                requestMethod = "GET"
                doInput = true

                // Check if the request was successful (status code 200)
                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    connect()
                    return@withContext inputStream // Return the InputStream when successful
                } else {
                    // Log the error if the request was not successful
                    Log.e(TAG, "HTTP request failed with status code $responseCode")
                }
            }

            // Return null if there was an error
            return@withContext null
        }
    }
}