package ddwucom.mobile.medispotter

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.DatePicker
import androidx.appcompat.app.AlertDialog
import ddwu.com.mobile.openapitest.network.hospitalDao
import ddwucom.mobile.medispotter.data.Hospital
import ddwucom.mobile.medispotter.databinding.ActivityHospitalDetailBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.sql.Time
import java.util.Calendar

class HospitalDetailActivity : AppCompatActivity() {
    lateinit var detailBinding: ActivityHospitalDetailBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        detailBinding = ActivityHospitalDetailBinding.inflate(layoutInflater)
        setContentView(detailBinding.root)
        var hospital: Hospital? =null
        detailBinding.alarmBtn.setOnClickListener{
            val calenderInstance = Calendar.getInstance()
            val hr = calenderInstance.get(Calendar.HOUR_OF_DAY)
            val min = calenderInstance.get(Calendar.MINUTE)
            val year = calenderInstance.get(Calendar.YEAR)
            val mon = calenderInstance.get(Calendar.MONTH)
            val date = calenderInstance.get(Calendar.DATE)

            val dialogView = layoutInflater.inflate(R.layout.activity_dialog, null)
            val alertDialog = AlertDialog.Builder(this)
                .setView(dialogView)
                .create()

            dialogView.findViewById<Button>(R.id.date).setOnClickListener {
                DatePickerDialog(this, object : DatePickerDialog.OnDateSetListener{
                    override fun onDateSet(p0: DatePicker?, p1: Int, p2: Int, p3: Int) {

                        dialogView.findViewById<Button>(R.id.date).text = "${p1}년 ${p2}월 ${p3}일"
                    }

                }, year,mon,date).show()
            }

            alertDialog.show()

            dialogView.findViewById<Button>(R.id.time).setOnClickListener {

                val onTimeListner = TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
                    if (view.isShown) {
                        calenderInstance.set(Calendar.HOUR_OF_DAY, hourOfDay)
                        calenderInstance.set(Calendar.MINUTE, minute)
                        dialogView.findViewById<Button>(R.id.time).text = "${hourOfDay}시 ${minute}분"
                    }
                }
                val timePickerDialog = TimePickerDialog(
                    this,
                    android.R.style.Theme_Holo_Light_Dialog_NoActionBar,
                    onTimeListner,
                    hr,
                    min,
                    true
                )

                timePickerDialog.setTitle("시간")
                timePickerDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
                timePickerDialog.show()

            }
            dialogView.findViewById<Button>(R.id.saveBtn).setOnClickListener {
                alertDialog.dismiss()
            }
        }

        val hospitalId = intent.getIntExtra("hospitalId", -1)

        if (hospitalId != -1) {
            // Now, you have the hospitalId, you can use it to fetch hospital details
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    hospital = hospitalDao.getHospitalById(hospitalId)
                    Log.d("www", "${hospital!!._id}")
                    detailBinding.name.text = hospital!!.name
                    detailBinding.addr.text = hospital!!.dutyAddr
                    detailBinding.type.text = hospital!!.dutyDivName
                    detailBinding.monTime.text = "${hospital!!.dutyTime1s} ~ ${hospital!!.dutyTime1c}"
                    detailBinding.tueTime.text = "${hospital!!.dutyTime2s} ~ ${hospital!!.dutyTime2c}"
                    detailBinding.wedTime.text = "${hospital!!.dutyTime3s} ~ ${hospital!!.dutyTime3c}"
                    detailBinding.thurTime.text = "${hospital!!.dutyTime4s} ~ ${hospital!!.dutyTime4c}"
                    detailBinding.friTime.text = "${hospital!!.dutyTime5s} ~ ${hospital!!.dutyTime5c}"
                    detailBinding.satTime.text = "${hospital!!.dutyTime6s} ~ ${hospital!!.dutyTime6c}"
                    detailBinding.sunTime.text = "${hospital!!.dutyTime7s} ~ ${hospital!!.dutyTime7c}"
                    detailBinding.num.text = hospital!!.dutyTel1
                    detailBinding.info.text = hospital!!.dutyInfo
                    detailBinding.favorite.isChecked = hospital!!.isFavorite == 1

                } catch (e: Exception) {
                    // Handle exceptions if necessary
                    e.printStackTrace()
                }
            }
        } else {
            Log.e("HospitalDetailActivity", "Invalid hospitalId")
        }

        detailBinding.reviewBtn.setOnClickListener{
            // Create an Intent
            CoroutineScope(Dispatchers.Main).launch{
                val def = async(Dispatchers.IO) {
                    var temp : Hospital? = null
                    try {
                        temp = hospitalDao.getHospitalById(hospitalId)
                    } catch (e: IOException) {
                        Log.d(TAG, e.message?: "null")
                        null
                    } catch (e: XmlPullParserException) {
                        Log.d(TAG, e.message?: "null")
                        null
                    }
                    temp
                }

                hospital = def.await()
                val intent = Intent(this@HospitalDetailActivity, ReviewActivity::class.java)
                // Put the Hospital object as an extra with the key "hospital"
                intent.putExtra("hospital", hospital)

                // Start the ReviewActivity
                startActivity(intent)
            }

        }
        detailBinding.favorite.setOnClickListener {
            // Create an Intent
            CoroutineScope(Dispatchers.Main).launch{
                Log.d("ADSD", detailBinding.favorite.isChecked.toString())
                hospitalDao.updateFavorite(hospitalId, detailBinding.favorite.isChecked)
            }
        }
    }
}