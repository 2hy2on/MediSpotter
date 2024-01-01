package ddwucom.mobile.medispotter

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import ddwucom.mobile.medispotter.data.Hospital
import ddwucom.mobile.medispotter.databinding.FragmentFavoriteBinding


import ddwucom.mobile.medispotter.network.hospitalDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException

class FavoriteFragment : Fragment() {
    lateinit var binding: FragmentFavoriteBinding
    var hospital: List<Hospital>? = null
    private val REQ_DETAIL = 200
    val adapter = HospitalAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFavoriteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val layout = LinearLayoutManager(requireContext())
        layout.orientation = LinearLayoutManager.VERTICAL

        binding.rcView.adapter = adapter
        binding.rcView.layoutManager = layout

        CoroutineScope(Dispatchers.Main).launch {
            val def = async(Dispatchers.IO) {
                try {
                   hospital = hospitalDao.getHospitalFavorite()
                } catch (e: IOException) {
                    e.printStackTrace()
                    null
                } catch (e: XmlPullParserException) {
                    e.printStackTrace()
                    null
                }
                hospital
            }

            adapter.hospitals = def.await()
            adapter.notifyDataSetChanged()
        }

        // 수정하기 위한 클릭
        adapter.setOnItemClickListener(object : HospitalAdapter.OnItemCLickListener {
            override fun onItemClick(view: View, position: Int) {
                // startActivityForResult 대신 startActivity 사용
                val intent = Intent(requireContext(), HospitalDetailActivity::class.java)
                intent.putExtra("hospitalId", hospital?.get(position)?._id)
                startActivity(intent)
            }
        })
    }

    override fun onResume() {
        super.onResume()

        CoroutineScope(Dispatchers.Main).launch {
            val def = async(Dispatchers.IO) {
                try {
                    hospital = hospitalDao.getHospitalFavorite()
                } catch (e: IOException) {
                    e.printStackTrace()
                    null
                } catch (e: XmlPullParserException) {
                    e.printStackTrace()
                    null
                }
                hospital
            }

            adapter.hospitals = def.await()
            adapter.notifyDataSetChanged()
        }
    }
}
