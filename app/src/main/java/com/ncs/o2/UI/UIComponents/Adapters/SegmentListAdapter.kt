package com.ncs.o2.UI.UIComponents.Adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ncs.o2.Domain.Models.Segment
import com.ncs.o2.Domain.Models.state.SegmentItem
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickThrottleBounceListener
import com.ncs.o2.databinding.SegmetSelectionItemBinding

/*
File : SegmentListAdapter.kt -> com.ncs.o2.UI.UIComponents.Adapters
Description : Adapter for segment list 

Author : Alok Ranjan (VC uname : apple)
Link : https://github.com/arpitmx
From : Bitpolarity x Noshbae (@Project : O2 Android)

Creation : 12:14 am on 09/06/23

Todo >
Tasks CLEAN CODE : 
Tasks BUG FIXES : 
Tasks FEATURE MUST HAVE : 
Tasks FUTURE ADDITION : 

*/
class SegmentListAdapter constructor(
    val segments: List<SegmentItem>,
    val onClickCallback: OnClickCallback
) : RecyclerView.Adapter<SegmentListAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            SegmetSelectionItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val segment = segments[position]

        holder.binding.segmentTitle.text = segment.segment_NAME
        holder.binding.root.setOnClickThrottleBounceListener {
            onClickCallback.onClick(segment, position)
        }
    }

    override fun getItemCount(): Int {
        return segments.size
    }

    inner class ViewHolder(val binding: SegmetSelectionItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    interface OnClickCallback {
        fun onClick(segment: SegmentItem, position: Int)
    }
}
