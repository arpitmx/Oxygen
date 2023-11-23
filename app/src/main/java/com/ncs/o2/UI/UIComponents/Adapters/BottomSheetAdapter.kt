package com.ncs.o2.UI.UIComponents.Adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickThrottleBounceListener
import com.ncs.o2.R
import com.ncs.o2.UI.UIComponents.BottomSheets.BottomSheet
import com.ncs.o2.databinding.BottomSheetEachItemBinding
import java.util.Random

class BottomSheetAdapter(
    private val dataList: MutableList<String>,
    private val type:String,
    private val context: Context,
    private val onClick: onClickString
) : RecyclerView.Adapter<BottomSheetAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            BottomSheetEachItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (type=="DIFFICULTY"){
            when(dataList[position]){
                "Easy"->holder.binding.bgColor.background=context.resources.getDrawable(R.drawable.label_cardview_green)
                "Medium"->holder.binding.bgColor.background=context.resources.getDrawable(R.drawable.label_cardview_yellow)
                "Hard"->holder.binding.bgColor.background=context.resources.getDrawable(R.drawable.label_cardview_red)
            }
            holder.binding.text.text=dataList[position].substring(0,1)
            holder.binding.textFull.text=dataList[position]
        }
        else{
            holder.binding.bgColor.setCardBackgroundColor(context.resources.getColor(R.color.purple))
            holder.binding.text.text=dataList[position].substring(0,1)
            holder.binding.textFull.text=dataList[position]
        }
        holder.binding.layout.setOnClickThrottleBounceListener{
            onClick.sendString(dataList[position],type)
        }
    }

    inner class ViewHolder(val binding: BottomSheetEachItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    interface onClickString {
        fun sendString(text: String,type: String)
    }
}
