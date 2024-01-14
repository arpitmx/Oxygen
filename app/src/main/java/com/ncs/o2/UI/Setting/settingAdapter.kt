package com.ncs.o2.UI.Setting

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ncs.o2.Domain.Utility.Codes
import com.ncs.o2.Domain.Utility.ExtensionsUtil.gone
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickThrottleBounceListener
import com.ncs.o2.Domain.Utility.ExtensionsUtil.visible
import com.ncs.o2.R

class settingAdater(
    private val items: List<Any>,
    private val OnSettingClick: onSettingClick) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val option = 1
        private const val title = 2
    }

    interface onSettingClick {
        fun onClick(position: Int)
    }

    class settingOptionViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.setting_title)
        var icon: ImageView = itemView.findViewById(R.id.setting_icon)
        var set_ver: TextView = itemView.findViewById(R.id.set_version)

    }

    class settingTitleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.settingName)
        val line: View = itemView.findViewById<View>(R.id.setting_line)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            option -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.settings_option_item, parent, false)
                settingOptionViewHolder(view)
            }

            title -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.settings_title_item, parent, false)
                settingTitleViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {


        when (holder.itemViewType) {
            option -> {
                val item = items[position] as settingOption
                val viewHolder = holder as settingOptionViewHolder
                viewHolder.textView.text = item.text
                viewHolder.set_ver.text = item.version

                item.Image?.let { viewHolder.icon.setImageResource(it) }

                viewHolder.itemView.setOnClickThrottleBounceListener {
                    Codes.STRINGS.clickedSetting = item.text
                    OnSettingClick.onClick(position)
                }
            }

            title -> {
                val item = items[position] as settingTitle
                val viewHolder = holder as settingTitleViewHolder
                viewHolder.textView.text = item.text

                if (position ==0){
                    viewHolder.line.gone()
                }else {
                    viewHolder.line.visible()
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is settingOption -> option
            is settingTitle -> title
            else -> throw IllegalArgumentException("Invalid data type at position $position")
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }
}
