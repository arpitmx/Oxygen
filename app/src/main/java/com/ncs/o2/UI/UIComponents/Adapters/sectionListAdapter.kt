package com.ncs.o2.UI.UIComponents.Adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ncs.o2.Domain.Models.Segment
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickThrottleBounceListener
import com.ncs.o2.UI.UIComponents.BottomSheets.sectionDisplayBottomSheet
import com.ncs.o2.databinding.SegmetSelectionItemBinding

class sectionListAdapter(
    val sections: List<*>,
    val onClickCallback: OnClickCallback
) : RecyclerView.Adapter<sectionListAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            SegmetSelectionItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val section = sections[position]

        holder.binding.segmentTitle.text = section.toString()
        holder.binding.root.setOnClickThrottleBounceListener {
            onClickCallback.onClick(section.toString())
        }
    }

    override fun getItemCount(): Int {
        return sections.size
    }

    interface OnClickCallback {
        fun onClick(sectionName: String)
    }

    inner class ViewHolder(val binding: SegmetSelectionItemBinding) :
        RecyclerView.ViewHolder(binding.root)

}
