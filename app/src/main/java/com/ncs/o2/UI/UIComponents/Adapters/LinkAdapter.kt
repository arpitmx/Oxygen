package com.ncs.o2.UI.UIComponents.Adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ncs.o2.Domain.Models.Tag
import com.ncs.o2.databinding.LinksItemBinding
import com.ncs.o2.databinding.TagItemBinding

class LinkAdapter constructor(private val linksList: List<String>) : RecyclerView.Adapter<LinkAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            LinksItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val linkItem = linksList[position]
        with(holder.binding.linksTv) {
            this.text = linkItem
        }
    }

    override fun getItemCount(): Int {
        return linksList.size
    }

    inner class ViewHolder(val binding: LinksItemBinding) :
        RecyclerView.ViewHolder(binding.root)
}