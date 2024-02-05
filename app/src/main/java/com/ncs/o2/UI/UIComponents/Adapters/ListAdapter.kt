package com.ncs.o2.UI.UIComponents.Adapters

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.ncs.o2.HelperClasses.PrefManager
import com.ncs.o2.R
import com.ncs.o2.UI.MainActivity
import com.ncs.versa.Constants.Endpoints

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

class RecyclerViewAdapter(private val context: Context, private val sList: List<String>) :
    RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>() {

    private val callback: ProjectCallback by lazy {
        context as MainActivity
    }

    private var selectedPosition = -1

    init {
        selectedPosition = PrefManager.getcurrentRadioButton()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.project_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return sList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindData(sList[position],position)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val label = itemView.findViewById<TextView>(R.id.project_title)
        private val radioButton = itemView.findViewById<RadioButton>(R.id.radioButton)
        private val layout = itemView.findViewById<LinearLayout>(R.id.layout)
        private val icon = itemView.findViewById<ImageView>(R.id.project_dp)

        fun bindData(item: String,position: Int) {
            label.text = item

            if (PrefManager.getProjectIconUrl(item) == "") {
                FirebaseFirestore.getInstance().collection(Endpoints.PROJECTS).document(item).get()
                    .addOnSuccessListener { documentSnapshot ->
                        if (documentSnapshot.exists()) {
                            val imageUrl = documentSnapshot.data?.get("ICON_URL")?.toString()

                            if (imageUrl != null && (context as? Activity)?.isDestroyed != true) {
                                PrefManager.setProjectIconUrl(item, imageUrl)
                                Glide.with(context)
                                    .load(imageUrl)
                                    .error(R.drawable.placeholder_image)
                                    .into(icon)
                            }
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.d("failCheck", exception.toString())
                    }
            } else {
                Glide.with(context)
                    .load(PrefManager.getProjectIconUrl(item))
                    .error(R.drawable.placeholder_image)
                    .into(icon)
            }

            radioButton.isChecked = position == selectedPosition
            layout.setOnClickListener {
                selectedPosition = position
                notifyDataSetChanged()
                callback.onClick(item, position)
            }
            radioButton.setOnClickListener {
                selectedPosition = position
                notifyDataSetChanged()
                callback.onClick(item, position)
            }
            label.setOnClickListener {
                selectedPosition = position
                notifyDataSetChanged()
                callback.onClick(item, position)
            }
        }
    }
}
