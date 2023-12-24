package ddwucom.mobile.medispotter

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.util.Log
import ddwu.com.mobile.openapitest.network.hospitalDao
import ddwucom.mobile.medispotter.data.Hospital
import ddwucom.mobile.medispotter.databinding.ActivityReviewBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReviewActivity : AppCompatActivity() {
    lateinit var reviewbinding: ActivityReviewBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        reviewbinding = ActivityReviewBinding.inflate(layoutInflater)
        setContentView(reviewbinding.root)
        var hospital: Hospital? =null

        val receivedIntent = intent
        if (receivedIntent != null) {
            hospital = receivedIntent.getSerializableExtra("hospital") as? Hospital
            if (hospital != null) {
                // Now you have the Hospital object in the receiving activity
                // Do something with it...
            } else {
                Log.e("ReviewActivity", "Invalid Hospital object received")
            }
        }
        reviewbinding.name.text = hospital?.name
        reviewbinding.rate.rating = hospital?.rating ?: 0.0f
        hospital?.review?.let { Log.d("DDDDDDDDD", it) }
        reviewbinding.review.setText(hospital?.review)

        reviewbinding.save.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                hospitalDao.updateHospitalRateAndReview(
                    hospital?._id,
                    reviewbinding.rate.rating,
                    reviewbinding.review.text.toString()
                )
            }
            finish()
         }
        reviewbinding.delete.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
               hospitalDao.deleteHospitalRateAndReview(hospital?._id)
            }
            finish()
        }


    }
}