package com.ncs.o2.UI.UIComponents.Adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ncs.o2.Domain.Models.CheckList
import com.ncs.o2.Domain.Utility.ExtensionsUtil.gone
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickThrottleBounceListener
import com.ncs.o2.Domain.Utility.ExtensionsUtil.visible
import com.ncs.o2.databinding.ChecklistItemBinding
import io.noties.markwon.Markwon

class CheckListAdapter constructor(private val list: MutableList<CheckList>,private val markwon: Markwon,private val checkListItemListener: CheckListItemListener,private val isCheckListCreation:Boolean) : RecyclerView.Adapter<CheckListAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ChecklistItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.number.text=(position+1).toString()
        holder.binding.titleTV.text=list[position].title
        markwon.setMarkdown(holder.binding.descTV, list[position].desc)
        if (isCheckListCreation){
            holder.binding.clear.visible()
            holder.binding.checkbox.gone()
            holder.binding.clear.setOnClickThrottleBounceListener {
                checkListItemListener.removeCheckList(position)
            }
        }
        if (!isCheckListCreation){
            holder.binding.clear.gone()
            holder.binding.checkbox.visible()

        }

        holder.binding.parent.setOnClickThrottleBounceListener {
            checkListItemListener.onClick(position)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class ViewHolder(val binding: ChecklistItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    interface CheckListItemListener{
        fun removeCheckList(position: Int)
        fun onClick(position: Int)
    }
}