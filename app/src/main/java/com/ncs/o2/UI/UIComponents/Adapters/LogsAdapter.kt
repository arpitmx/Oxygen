package com.ncs.o2.UI.UIComponents.Adapters


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ncs.o2.UI.Logs.LogsActivity
import com.ncs.o2.databinding.LogsRecyclerViewItemBinding

class LogsAdapter(
    private val dataList: MutableList<LogsActivity.LogsItem>,
) : RecyclerView.Adapter<LogsAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            LogsRecyclerViewItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.title.text=dataList[position].title
        holder.binding.readcount.text=dataList[position].count
    }

    inner class ViewHolder(val binding: LogsRecyclerViewItemBinding) :
        RecyclerView.ViewHolder(binding.root)

}
