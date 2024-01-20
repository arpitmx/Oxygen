package com.ncs.o2.UI.Teams

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ncs.o2.databinding.ChannelsEachItemBinding

class ChannelsAdapter(
    private val dataList: MutableList<String>,
) : RecyclerView.Adapter<ChannelsAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ChannelsEachItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.binding.channelName.text=dataList[position]

    }

    inner class ViewHolder(val binding: ChannelsEachItemBinding) :
        RecyclerView.ViewHolder(binding.root)


}
