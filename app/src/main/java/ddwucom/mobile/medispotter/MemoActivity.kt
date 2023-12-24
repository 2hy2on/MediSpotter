package ddwucom.mobile.medispotter

import android.R
import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.recyclerview.widget.LinearLayoutManager
import ddwu.com.mobile.openapitest.network.hospitalDao
import ddwucom.mobile.medispotter.data.Hospital
import ddwucom.mobile.medispotter.databinding.ActivityMemoBinding
import ddwucom.mobile.medispotter.databinding.ActivityReviewBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException

class MemoActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {
    lateinit var memoBinding: ActivityMemoBinding
    var hospital: List<Hospital>? =null
    private val REQ_DETAIL = 200
    val adapter = HospitalAdapter()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        memoBinding = ActivityMemoBinding.inflate(layoutInflater)
        setContentView(memoBinding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

//        hospitals.add(Hospital("안녕", "zzrz", "2222", "22222","한국"))
//        hospitals.add(Hospital("안녕", "zzrz", "2222", "22222","한국"))
//        hospitals.add(Hospital("안녕", "zzrz", "2222", "22222","한국"))
//        hospitals.add(Hospital("안녕", "zzrz", "2222", "22222","한국"))
//        hospitals.add(Hospital("안녕", "zzrz", "2222", "22222","한국"))
//        hospitals.add(Hospital("안녕", "zzrz", "2222", "22222","한국"))
//        hospitals.add(Hospital("안녕", "zzrz", "2222", "22222","한국"))
//        hospitals.add(Hospital("안녕", "zzrz", "2222", "22222","한국"))

        //spinner
        val spinner = memoBinding.select
        val items = arrayOf("리뷰", "즐겨찾기", "알람")

        spinner.onItemSelectedListener = this
        val sAdapter = ArrayAdapter(this, R.layout.simple_spinner_item, items)
        sAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = sAdapter


        val layout = LinearLayoutManager(this@MemoActivity)
        layout.orientation = LinearLayoutManager.VERTICAL

        memoBinding.rcView.adapter = adapter
        memoBinding.rcView.layoutManager = layout

        CoroutineScope(Dispatchers.Main).launch{
            val def = async(Dispatchers.IO) {

                try {
                    hospital = hospitalDao.getHospitalReview()
                } catch (e: IOException) {
                    Log.d(TAG, e.message?: "null")
                    null
                } catch (e: XmlPullParserException) {
                    Log.d(TAG, e.message?: "null")
                    null
                }
                hospital
            }

            adapter.hospitals = def.await()
           adapter.notifyDataSetChanged()

        }
        //수정하기 위한 클릭
        adapter.setOnItemClickListener(object :HospitalAdapter.OnItemCLickListener{
            override fun onItemClick(view: View, position: Int) {
                val intent = Intent(this@MemoActivity, HospitalDetailActivity::class.java)
                intent.putExtra("hospitalId", hospital?.get(position)?._id)
                startActivityForResult(intent, REQ_DETAIL)
//                startActivity(intent)
            }

        })
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        when(parent?.id){
            memoBinding.select.id->{
                val selectedItem = parent.getItemAtPosition(position).toString()
                when (selectedItem) {
                    "리뷰" -> {
                        CoroutineScope(Dispatchers.Main).launch{
                            val def = async(Dispatchers.IO) {

                                try {
                                    hospital = hospitalDao.getHospitalReview()
                                } catch (e: IOException) {
                                    Log.d(TAG, e.message?: "null")
                                    null
                                } catch (e: XmlPullParserException) {
                                    Log.d(TAG, e.message?: "null")
                                    null
                                }
                                hospital
                            }

                            adapter.hospitals = def.await()
                            adapter.notifyDataSetChanged()

                        }
                    }
                    "즐겨찾기" -> {
                        CoroutineScope(Dispatchers.Main).launch{
                            val def = async(Dispatchers.IO) {

                                try {
                                    hospital = hospitalDao.getHospitalFavorite()
                                } catch (e: IOException) {
                                    Log.d(TAG, e.message?: "null")
                                    null
                                } catch (e: XmlPullParserException) {
                                    Log.d(TAG, e.message?: "null")
                                    null
                                }
                                hospital
                            }

                            adapter.hospitals = def.await()
                            adapter.notifyDataSetChanged()

                        }
                    }
                    "알람"->{

                    }
                }
            }
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        TODO("Not yet implemented")
    }

}

