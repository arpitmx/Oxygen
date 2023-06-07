package com.ncs.o2.UI.UIComponents.Adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ncs.o2.Domain.Models.Tag
import com.ncs.o2.databinding.TagItemBinding

/*
File : TagAdapter.kt -> com.ncs.o2.Adapters
Description : Adapter class for tags 

Author : Alok Ranjan (VC uname : apple)
Link : https://github.com/arpitmx
From : Bitpolarity x Noshbae (@Project : O2 Android)

Creation : 11:50 pm on 04/06/23

Todo >
Tasks CLEAN CODE : 
Tasks BUG FIXES : 
Tasks FEATURE MUST HAVE : 
Tasks FUTURE ADDITION : 

*/
class TagAdapter constructor(private val tagList: List<Tag>) : RecyclerView.Adapter<TagAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            TagItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val tagItem = tagList[position]

        with(holder.binding.tag) {
            this.text = tagItem.tagText
            this.setBackgroundColor(Color.parseColor(tagItem.bgColor))
            this.setTextColor(Color.parseColor(tagItem.textColor))
        }
    }

    override fun getItemCount(): Int {
        return tagList.size
    }

    inner class ViewHolder(val binding: TagItemBinding) :
        RecyclerView.ViewHolder(binding.root)
}