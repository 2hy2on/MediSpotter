package ddwucom.mobile.medispotter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ddwucom.mobile.medispotter.data.Hospital
import ddwucom.mobile.medispotter.databinding.ListHostpitalBinding

class HospitalAdapter:
RecyclerView.Adapter<HospitalAdapter.HospitalViewHolder>(){
    var hospitals: List<Hospital>?=null

    interface OnItemCLickListener{
        fun onItemClick(view: View, position:Int)
    }
    lateinit var listener: OnItemCLickListener

    fun setOnItemClickListener(listener: OnItemCLickListener){
        this.listener = listener
    }



    class HospitalViewHolder(val itemBinding: ListHostpitalBinding, listener: OnItemCLickListener): RecyclerView.ViewHolder(itemBinding.root){
        init {
            itemBinding.root.setOnClickListener {
                listener.onItemClick(it, adapterPosition)
            }

        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HospitalViewHolder {
        val itemBinding = ListHostpitalBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HospitalViewHolder(itemBinding, listener)
    }

    override fun onBindViewHolder(holder: HospitalViewHolder, position: Int) {
        holder.itemBinding.name.text = hospitals?.get(position)?.name
        holder.itemBinding.type.text = hospitals?.get(position)?.dutyDivName
        holder.itemBinding.favorite.isChecked = hospitals?.get(position)?.isFavorite == 1

    }

    override fun getItemCount(): Int {
        return hospitals?.size ?: 0
    }

}