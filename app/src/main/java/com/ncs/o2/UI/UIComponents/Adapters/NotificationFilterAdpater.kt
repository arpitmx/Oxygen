package com.ncs.o2.UI.UIComponents.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickThrottleBounceListener
import com.ncs.o2.R
import com.ncs.o2.UI.UIComponents.BottomSheets.FilterNotificationsBottomSheet
import com.ncs.o2.databinding.BottomSheetEachItemBinding
import com.ncs.o2.databinding.FilterItemBinding

class NotificationFilterAdpater(
    private val dataList: MutableList<FilterNotificationsBottomSheet.FilterNotifications>,
    private val onFilterClick: OnFilterClick
) : RecyclerView.Adapter<NotificationFilterAdpater.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            FilterItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var filter = dataList[position]

        holder.binding.title.text=dataList[position].title
        holder.binding.radioButton.isChecked=dataList[position].isChecked

        holder.binding.radioButton.setOnCheckedChangeListener(null)

        holder.binding.radioButton.setOnCheckedChangeListener { btn, isChecked ->
            dataList[position].isChecked = isChecked

            if (isChecked) {
                for (i in dataList.indices) {
                    if (i != position) {
                        dataList[i].isChecked = false
                        notifyItemChanged(i)
                    }
                }
            }

            onFilterClick.onClick(filter, position, isChecked)
        }



        holder.binding.root.setOnClickListener {
            val isCheckedC = holder.binding.radioButton.isChecked
            if (!isCheckedC) {
                holder.binding.radioButton.isChecked = true
                filter.isChecked = true
                onFilterClick.onClick(filter,position,true)
            }
        }

    }

    inner class ViewHolder(val binding: FilterItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    interface OnFilterClick{
        fun onClick(filter:FilterNotificationsBottomSheet.FilterNotifications,position: Int,isChecked:Boolean)
    }

}
