package com.ncs.o2.Adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ncs.o2.Models.Tag
import com.ncs.o2.R

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
        val view = LayoutInflater.from(parent.context).inflate(R.layout.tag_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val tagItem = tagList[position]
        holder.tagRoot.text = tagItem.tagText
        holder.tagRoot.setBackgroundColor(Color.parseColor(tagItem.bgColor))
        holder.tagRoot.setTextColor(Color.parseColor(tagItem.textColor))

    }

    override fun getItemCount(): Int {
        return tagList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tagRoot: TextView = itemView.findViewById(R.id.tag)
    }
}