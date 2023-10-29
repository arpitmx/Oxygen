package com.ncs.o2.UI.UIComponents.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.TextView
import com.ncs.o2.HelperClasses.PrefManager
import com.ncs.o2.R
import com.ncs.o2.UI.MainActivity

/*
File : ListAdapter.kt -> com.ncs.o2
Description : Adapter for list  

Author : Alok Ranjan (VC uname : apple)
Link : https://github.com/arpitmx
From : Bitpolarity x Noshbae (@Project : O2 Android)

Creation : 3:03 am on 31/05/23

Todo >
Tasks CLEAN CODE : 
Tasks BUG FIXES : 
Tasks FEATURE MUST HAVE : 
Tasks FUTURE ADDITION : 


*/

interface ProjectCallback{
    fun onClick(projectID : String,position: Int)
}


 class ListAdapter(context: Context, val sList : List<String>) : BaseAdapter() {
    private  val mInflator: LayoutInflater
    private val callback : ProjectCallback by lazy {
        context as MainActivity
    }

     private var selectedPosition = -1


     init {
        this.mInflator = LayoutInflater.from(context)
         PrefManager.initialize(context)
         selectedPosition=PrefManager.getcurrentRadioButton()
    }

    override fun getCount(): Int {
        return sList.size
    }

    override fun getItem(position: Int): Any {
        return sList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
        val view: View?
        val vh: ListRowHolder
        if(convertView == null) {
            view = this.mInflator.inflate(R.layout.project_list_item, parent, false)
            vh = ListRowHolder(view)
            view.tag = vh

        } else {
            view = convertView
            vh = view.tag as ListRowHolder
        }

        vh.label.text = sList[position]
        vh.radioButton.isChecked = position == selectedPosition
        vh.layout.setOnClickListener {
            selectedPosition = position
            notifyDataSetChanged()
            callback.onClick(sList[position], position)
        }
        vh.radioButton.setOnClickListener {
            selectedPosition = position
            notifyDataSetChanged()
            callback.onClick(sList[position], position)
        }

        vh.label.setOnClickListener{
            selectedPosition = position
            notifyDataSetChanged()
            callback.onClick(sList[position], position)
        }

        return view
    }
}

private class ListRowHolder(row: View?) {
     var label: TextView
     var radioButton:RadioButton
     var layout:LinearLayout

    init {
        this.label = row?.findViewById(R.id.project_title) as TextView
        this.radioButton= row.findViewById(R.id.radioButton) as RadioButton
        this.layout=row.findViewById(R.id.layout) as LinearLayout
    }



}
