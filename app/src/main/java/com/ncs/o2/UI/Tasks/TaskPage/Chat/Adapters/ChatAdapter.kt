package com.ncs.o2.UI.Tasks.TaskPage.Chat.Adapters

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.google.firebase.Timestamp
import com.ncs.o2.Data.Room.MessageRepository.MessageDatabase
import com.ncs.o2.Data.Room.MessageRepository.UsersDao
import com.ncs.o2.Domain.Models.Enums.MessageType
import com.ncs.o2.Domain.Models.Message
import com.ncs.o2.Domain.Models.ServerResult
import com.ncs.o2.Domain.Models.User
import com.ncs.o2.Domain.Models.UserInMessage
import com.ncs.o2.Domain.Repositories.FirestoreRepository
import com.ncs.o2.Domain.Utility.ExtensionsUtil.gone
import com.ncs.o2.R
import com.ncs.o2.databinding.UserMessageItemBinding
import com.ncs.versa.Constants.Endpoints
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date


/*
File : ChatAdapter.kt -> com.ncs.nyayvedika.UI.Chat.Adapters
Description : Adapter for chats

Author : Alok Ranjan (VC uname : apple)
Link : https://github.com/arpitmx
From : Bitpolarity x Noshbae (@Project : NyayVedika Android)

Creation : 5:46 pm on 16/09/23

Todo >
Tasks CLEAN CODE :
Tasks BUG FIXES :
Tasks FEATURE MUST HAVE :
Tasks FUTURE ADDITION :

*/

class ChatAdapter(val repository: FirestoreRepository, var msgList: MutableList<Message>, val context : Context) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var messageDatabase: MessageDatabase
    var db: UsersDao

    init {
        messageDatabase = Room.databaseBuilder(context, MessageDatabase::class.java, Endpoints.ROOM.MESSAGES.USERLIST_DB).build()
        db=messageDatabase.usersDao()
        msgList.sortBy { it.timestamp?.toDate() }
    }
    companion object {
        const val NORMAL_MSG = 0
        const val IMAGE_MSG = 1
        const val FILE_MSG = 2
    }

    private inner class UserMessage_ViewHolder( val binding: UserMessageItemBinding) :
        RecyclerView.ViewHolder(binding.root){


        fun bind(position: Int){
            CoroutineScope(Dispatchers.Main).launch {
                if (db.getUserbyId(msgList[position].senderId) == null) {
                    fetchUser(msgList[position].senderId){
                        CoroutineScope(Dispatchers.Main).launch {
                            Log.d("DB","This a new user, now saving in DB....")
                            db.insertUser(it)
                            var user=it
                            val msg = msgList.get(position).content
                            binding.message.setText(msg)
                            val timeDifference = Date().time - msgList[position].timestamp!!.toDate().time
                            val minutes = (timeDifference / (1000 * 60)).toInt()
                            val hours = minutes / 60
                            val days = hours / 24
                            val weeks = days / 7
                            val months = days / 30
                            val years = days / 365

                            val timeAgo: String = when {
                                years > 0 -> "about $years ${if (years == 1) "year" else "years"} ago"
                                months > 0 -> "about $months ${if (months == 1) "month" else "months"} ago"
                                weeks > 0 -> "about $weeks ${if (weeks == 1) "week" else "weeks"} ago"
                                days > 0 -> "about $days ${if (days == 1) "day" else "days"} ago"
                                hours > 0 -> "about $hours ${if (hours == 1) "hour" else "hours"} ago"
                                minutes > 0 -> "about $minutes ${if (minutes == 1) "minute" else "minutes"} ago"
                                else -> "just now"
                            }

                            binding.timeStamp.text=timeAgo
                            Glide.with(binding.root)
                                .load(user.DP_URL)
                                .listener(object : RequestListener<Drawable> {

                                    override fun onLoadFailed(
                                        e: GlideException?,
                                        model: Any?,
                                        target: Target<Drawable>?,
                                        isFirstResource: Boolean
                                    ): Boolean {
                                        return false
                                    }
                                    override fun onResourceReady(
                                        resource: Drawable?,
                                        model: Any?,
                                        target: Target<Drawable>?,
                                        dataSource: DataSource?,
                                        isFirstResource: Boolean
                                    ): Boolean {
                                        return false
                                    }
                                })
                                .encodeQuality(80)
                                .override(40,40)
                                .apply(
                                    RequestOptions().
                                    diskCacheStrategy(DiskCacheStrategy.ALL)
                                )
                                .error(R.drawable.profile_pic_placeholder)
                                .into(binding.userDp)
                            binding.userName.text=user.USERNAME
                        }

                    }
                }
                if (db.getUserbyId(msgList[position].senderId)!=null){
                    Log.d("DB","This is a returning user, retrieving from DB....")

                    var user= db.getUserbyId(msgList[position].senderId)!!
                    val msg = msgList.get(position).content
                    binding.message.setText(msg)
                    val timeDifference = Date().time - msgList[position].timestamp!!.toDate().time
                    val minutes = (timeDifference / (1000 * 60)).toInt()
                    val hours = minutes / 60
                    val days = hours / 24
                    val weeks = days / 7
                    val months = days / 30
                    val years = days / 365

                    val timeAgo: String = when {
                        years > 0 -> "about $years ${if (years == 1) "year" else "years"} ago"
                        months > 0 -> "about $months ${if (months == 1) "month" else "months"} ago"
                        weeks > 0 -> "about $weeks ${if (weeks == 1) "week" else "weeks"} ago"
                        days > 0 -> "about $days ${if (days == 1) "day" else "days"} ago"
                        hours > 0 -> "about $hours ${if (hours == 1) "hour" else "hours"} ago"
                        minutes > 0 -> "about $minutes ${if (minutes == 1) "minute" else "minutes"} ago"
                        else -> "just now"
                    }

                    binding.timeStamp.text=timeAgo
                    Glide.with(binding.root)
                        .load(user.DP_URL)
                        .listener(object : RequestListener<Drawable> {

                            override fun onLoadFailed(
                                e: GlideException?,
                                model: Any?,
                                target: Target<Drawable>?,
                                isFirstResource: Boolean
                            ): Boolean {
                                return false
                            }
                            override fun onResourceReady(
                                resource: Drawable?,
                                model: Any?,
                                target: Target<Drawable>?,
                                dataSource: DataSource?,
                                isFirstResource: Boolean
                            ): Boolean {
                                return false
                            }
                        })
                        .encodeQuality(80)
                        .override(40,40)
                        .apply(
                            RequestOptions().
                            diskCacheStrategy(DiskCacheStrategy.ALL)
                        )
                        .error(R.drawable.profile_pic_placeholder)
                        .into(binding.userDp)
                    binding.userName.text=user.USERNAME
                }
            }

        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {

            NORMAL_MSG -> {
                UserMessage_ViewHolder(UserMessageItemBinding.inflate(LayoutInflater.from(context),parent,false))
            }

            else -> {
                throw IllegalArgumentException("Invalid view type")
            }
        }
    }

    override fun getItemCount(): Int {
        return msgList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        (holder as UserMessage_ViewHolder).bind(position)

    }

    public fun appendMessage(msg:Message){
        msgList.add(msg)
        notifyDataSetChanged()
    }

    public fun showTyping(show:Boolean){
       if (show){
           msgList.add(Message("123","123","Typing...", Timestamp.now(), MessageType.NORMAL_MSG, emptyMap()))
           notifyDataSetChanged()
       }else{
           msgList.removeAt(msgList.size-1)
           notifyDataSetChanged()
       }
    }
    private fun fetchUser(user_id: String, onUserFetched: (UserInMessage) -> Unit) {
        repository.getMessageUserInfobyId(user_id) { result ->
            when (result) {
                is ServerResult.Success -> {
                    val user = result.data
                    if (user != null) {
                        onUserFetched(user)
                    }
                }
                is ServerResult.Failure -> {

                }
                is ServerResult.Progress->{

                }
            }
        }
    }

}