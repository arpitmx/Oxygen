package com.ncs.o2.UI.UIComponents.Adapters

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Handler
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import com.ncs.o2.Domain.Models.CheckList
import com.ncs.o2.Domain.Utility.ExtensionsUtil.gone
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickThrottleBounceListener
import com.ncs.o2.Domain.Utility.ExtensionsUtil.visible
import com.ncs.o2.R
import com.ncs.o2.databinding.ChecklistItemBinding
import io.noties.markwon.Markwon

class CheckListAdapter constructor(private val list: MutableList<CheckList>,private val markwon: Markwon,private val checkListItemListener: CheckListItemListener,
                                   private val isCheckListCreation:Boolean, private val isModerator:Boolean = false,
                                   private val isAssignee:Boolean=false) : RecyclerView.Adapter<CheckListAdapter.ViewHolder>() {
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
            holder.binding.parent.setOnClickThrottleBounceListener {
                checkListItemListener.onClick(position)
            }
        }
        if (!isCheckListCreation){
            if (!isAssignee && !isModerator){
                holder.binding.clear.gone()
                holder.binding.checkbox.gone()
                val isChecked=list[position].done
                if (isChecked) {
                    holder.binding.completed.visible()
                }
                if(!isChecked) {
                    holder.binding.completed.gone()
                }
            }
            if (isAssignee){
                holder.binding.clear.gone()
                holder.binding.checkbox.visible()
                holder.binding.checkbox.isChecked=list[position].done
                val isChecked=holder.binding.checkbox.isChecked
                if (isChecked) {
                    holder.binding.completed.visible()
                }
                if(!isChecked) {
                    holder.binding.completed.gone()

                }
                holder.binding.checkbox.setOnCheckedChangeListener{
                        btn,isChecked->
                    if (isChecked) {
                        holder.binding.completed.visible()
                    }
                    if(!isChecked) {
                        holder.binding.completed.gone()

                    }
                    checkListItemListener.onCheckBoxClick(list[position].id, isChecked,position)
                }
            }
            if (isAssignee && isModerator){
                holder.binding.clear.gone()
                holder.binding.checkbox.visible()
                holder.binding.checkbox.isChecked=list[position].done
                val isChecked=holder.binding.checkbox.isChecked
                if (isChecked) {
                    holder.binding.completed.visible()
                }
                if(!isChecked) {
                    holder.binding.completed.gone()

                }
                holder.binding.checkbox.setOnCheckedChangeListener{
                        btn,isChecked->
                    if (isChecked) {
                        holder.binding.completed.visible()
                    }
                    if(!isChecked) {
                        holder.binding.completed.gone()

                    }
                    checkListItemListener.onCheckBoxClick(list[position].id, isChecked,position)
                }
                holder.binding.parent.setOnClickThrottleBounceListener {
                    checkListItemListener.onClick(position)
                }
            }
            if (isModerator){
                holder.binding.clear.gone()
                holder.binding.checkbox.visible()
                holder.binding.checkbox.isChecked=list[position].done
                val isChecked=holder.binding.checkbox.isChecked
                if (isChecked) {
                    holder.binding.completed.visible()
                }
                if(!isChecked) {
                    holder.binding.completed.gone()

                }
                holder.binding.checkbox.setOnCheckedChangeListener{
                        btn,isChecked->
                    if (isChecked) {
                        holder.binding.completed.visible()
                    }
                    if(!isChecked) {
                        holder.binding.completed.gone()

                    }
                    checkListItemListener.onCheckBoxClick(list[position].id, isChecked,position)
                }
                holder.binding.parent.setOnClickThrottleBounceListener {
                    checkListItemListener.onClick(position)
                }
            }
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
        fun onCheckBoxClick(id:String,isChecked:Boolean,position: Int)
    }
    fun updateCheckBoxState(id: String, isChecked: Boolean) {
        val item = list.firstOrNull { it.id == id }
        item?.done = isChecked
        notifyItemChanged(list.indexOf(item))
    }
}