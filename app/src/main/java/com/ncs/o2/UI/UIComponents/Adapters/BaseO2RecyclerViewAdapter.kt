package com.ncs.o2.UI.UIComponents.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.recyclerview.widget.RecyclerView.inflate
import androidx.viewbinding.ViewBinding
import java.util.zip.Inflater

/*
File : BaseO2RecyclerViewAdapter.kt -> com.ncs.o2.UI.UIComponents.Adapters
Description : Base adapter for recyclerview 

Author : Alok Ranjan (VC uname : apple)
Link : https://github.com/arpitmx
From : Bitpolarity (@Project : O2 Android)

Creation : 12:28 pm on 28/06/23

Todo >
Tasks CLEAN CODE : 
Tasks BUG FIXES : 
Tasks FEATURE MUST HAVE : 
Tasks FUTURE ADDITION : 

*/


abstract class BaseO2RecyclerViewAdapter<T : Any ,VB :ViewDataBinding>
    : RecyclerView.Adapter
    <BaseO2RecyclerViewAdapter.Companion.BaseViewHolder<VB>>()
{

    var items = mutableListOf<T>()

    fun addItems (items: List<T>){
        this.items = items as MutableList<T>
        notifyDataSetChanged()
    }

    abstract fun getItemLayout() : Int

    var listener: ((view: View, item: T, position: Int) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)
    = BaseViewHolder<VB>(
        DataBindingUtil.inflate(
                 LayoutInflater.from(parent.context),
                 getItemLayout(),
                 parent,
            false
        )
    )


    override fun getItemCount(): Int = items.size


    companion object {
        class BaseViewHolder<VB : ViewDataBinding>(val binding: VB):
            ViewHolder(binding.root)

    }
}