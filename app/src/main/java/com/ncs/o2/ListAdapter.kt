package com.ncs.o2

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import android.widget.Toast
import com.ncs.o2.UI.MainActivity
import kotlinx.coroutines.flow.callbackFlow

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
    fun onClick(projectID : String)
}


 class ListAdapter(context: Context, val sList : List<String>) : BaseAdapter() {
    private  val mInflator: LayoutInflater
    private val callback : ProjectCallback by lazy {
        context as MainActivity
    }


     init {
        this.mInflator = LayoutInflater.from(context)
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
        vh.label.setOnClickListener{
            callback.onClick(position.toString())
        }
        return view
    }
}

private class ListRowHolder(row: View?) {
     var label: TextView

    init {
        this.label = row?.findViewById(R.id.project_title) as TextView
    }



}
