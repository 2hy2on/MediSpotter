package ddwu.com.mobile.openapitest.network

import android.content.Context
import android.util.Log
import ddwucom.mobile.medispotter.R
import ddwucom.mobile.medispotter.data.Hospital
import ddwucom.mobile.medispotter.network.HospitalParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import kotlin.jvm.Throws
import kotlin.text.StringBuilder


class NetworkManager(val context: Context) {
    private val TAG = "NetworkManager"

    val openApiUrl by lazy {
        context.resources.getString(R.string.hospital_url)
    }

    @Throws(IOException::class)
    suspend fun downloadXml(location1: String, lacation2: String) : List<Hospital>? {    val urlBuilder = StringBuilder(openApiUrl)
        urlBuilder.append("?"+ URLEncoder.encode("serviceKey", "UTF-8") + "=VgnAmvRIZCJfNOvf4sf9%2BXH53u6BdwKRdv9bixebAuK1tCRqJlVT%2B0Do76KmIUP3uz1yAmjjRZm6DPYa9iM%2FeA%3D%3D") /*Service Key*/
        urlBuilder.append("&" + URLEncoder.encode("Q0", "UTF-8") + "=" + URLEncoder.encode(location1, "UTF-8")) /*주소(시도)*/
        urlBuilder.append("&" + URLEncoder.encode("Q1", "UTF-8") + "=" + URLEncoder.encode(lacation2, "UTF-8")) /*주소(시군구)*/

        var hospitals: List<Hospital>? = null
        val inputStream = downloadUrl(urlBuilder)
        val parser = HospitalParser()
//        // Check if the inputStream is not null before reading it
        inputStream?.let {
            hospitals = parser.parse(it)
            Log.d("parse 끝나고 되돌아옴", hospitals.toString())
            }
//
//        // Close the InputStream after reading its content
//        inputStream?.close()
        /*Parser 생성 및 parsing 수행*/
//        inputStream?.use {
//            /*Parser 생성 및 parsing 수행*/
//            val parser = HospitalParser()
//            hospitals = parser.parse(it)
//        }

        return hospitals
    }


    @Throws(IOException::class)
    private suspend fun downloadUrl(urlString: StringBuilder) : InputStream? {
        return withContext(Dispatchers.IO) {
            val url = URL(urlString.toString())
            Log.d("DSd", url.toString())
            (url.openConnection() as? HttpURLConnection)?.run {
                readTimeout = 10000
                connectTimeout = 10000
                requestMethod = "GET"
                doInput = true

                // Check if the request was successful (status code 200)
                if (responseCode == HttpURLConnection.HTTP_OK) {
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


    // InputStream 을 String 으로 변환
// InputStream 을 String 으로 변환
    private fun readStreamToString(iStream: InputStream?): String {
        val resultBuilder = StringBuilder()

        iStream?.use { inputStream ->
            val inputStreamReader = InputStreamReader(inputStream)
            val bufferedReader = BufferedReader(inputStreamReader)

            var readLine: String? = bufferedReader.readLine()
            while (readLine != null) {
                resultBuilder.append(readLine + System.lineSeparator())
                readLine = bufferedReader.readLine()
            }
        }

        return resultBuilder.toString()
    }


    // InputStream 을 Bitmap 으로 변환
//    private fun readStreamToImage(iStream: InputStream?) : Bitmap {
//        val bitmap = BitmapFactory.decodeStream(iStream)
//        return bitmap
//    }
}