package ddwucom.mobile.medispotter

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import ddwucom.mobile.medispotter.data.Hospital
import ddwucom.mobile.medispotter.databinding.FragmentReviewBinding
import ddwucom.mobile.medispotter.network.hospitalDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException

class ReviewFragment : Fragment() {
    private var _binding: FragmentReviewBinding? = null
    private val binding get() = _binding!!

    var hospital: List<Hospital>? = null
    private val REQ_DETAIL = 200
    val adapter = HospitalAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentReviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()

        CoroutineScope(Dispatchers.Main).launch {
            val def = async(Dispatchers.IO) {
                try {
                    hospital = hospitalDao.getHospitalReview()
                } catch (e: IOException) {
                    Log.d(TAG, e.message ?: "null")
                    null
                } catch (e: XmlPullParserException) {
                    Log.d(TAG, e.message ?: "null")
                    null
                }
                hospital
            }

            adapter.hospitals = def.await()
            adapter.notifyDataSetChanged()
        }

        adapter.setOnItemClickListener(object : HospitalAdapter.OnItemCLickListener {
            override fun onItemClick(view: View, position: Int) {
                val intent = Intent(requireContext(), HospitalDetailActivity::class.java)
                intent.putExtra("hospitalId", hospital?.get(position)?._id)
                startActivity(intent)
            }
        })
    }

    private fun setupRecyclerView() {
        val layout = LinearLayoutManager(requireContext())
        layout.orientation = LinearLayoutManager.VERTICAL

        binding.rcView.adapter = adapter
        binding.rcView.layoutManager = layout
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()

        // Launch the coroutine every time the fragment is resumed
        CoroutineScope(Dispatchers.Main).launch {
            val def = async(Dispatchers.IO) {
                try {
                    hospital = hospitalDao.getHospitalReview()
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
