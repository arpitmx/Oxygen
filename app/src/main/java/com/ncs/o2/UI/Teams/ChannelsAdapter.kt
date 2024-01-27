package com.ncs.o2.UI.Teams

import android.view.LayoutInflater
import android.view.ScrollCaptureCallback
import android.view.ViewGroup
import androidx.core.app.NotificationCompat.MessagingStyle.Message
import androidx.core.view.get
import androidx.recyclerview.widget.RecyclerView
import com.ncs.o2.Constants.Pref
import com.ncs.o2.Data.Room.MessageRepository.MessageDatabase
import com.ncs.o2.Domain.Models.Channel
import com.ncs.o2.Domain.Utility.ExtensionsUtil.gone
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickThrottleBounceListener
import com.ncs.o2.Domain.Utility.ExtensionsUtil.visible
import com.ncs.o2.HelperClasses.PrefManager
import com.ncs.o2.databinding.ChannelsEachItemBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChannelsAdapter(
    private val dataList: MutableList<Channel>,
    private val callback: OnClick,
    private val db:MessageDatabase
) : RecyclerView.Adapter<ChannelsAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ChannelsEachItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val currentProject=PrefManager.getcurrentProject()
        val currentChannel=dataList[position].channel_name
        holder.binding.channelName.text=dataList[position].channel_name

        val lastTimestamp=PrefManager.getChannelNotiTimestamp(currentProject,currentChannel)

        CoroutineScope(Dispatchers.IO).launch {
            var count=0
            val msgList=db.teamsMessagesDao().getMessagesForProject(currentProject,currentChannel)
            for (msg in msgList.sortedByDescending { it.timestamp }){
                if (msg.timestamp!! >lastTimestamp){
                    count++
                }
            }
            withContext(Dispatchers.Main){
                if (count in 1..99){
                    holder.binding.notificationCount.text=count.toString()
                    holder.binding.notificationCountParent.visible()
                }
                if (count>=100){
                    holder.binding.notificationCount.text="100+"
                    holder.binding.notificationCountParent.visible()
                }
            }
        }

        holder.binding.parent.setOnClickThrottleBounceListener{
            callback.onChannelClick(dataList[position])
            holder.binding.notificationCount.text="0"
            holder.binding.notificationCountParent.gone()

        }

    }

    inner class ViewHolder(val binding: ChannelsEachItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    interface OnClick{
        fun onChannelClick(channel: Channel)
    }


}
