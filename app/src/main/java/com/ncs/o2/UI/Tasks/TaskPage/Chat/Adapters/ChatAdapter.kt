package com.ncs.o2.UI.Tasks.TaskPage.Chat.Adapters

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import br.tiagohm.markdownview.css.InternalStyleSheet
import br.tiagohm.markdownview.css.styles.Github
import com.ncs.o2.Data.Room.MessageRepository.MessageDatabase
import com.ncs.o2.Data.Room.MessageRepository.UsersDao
import com.ncs.o2.Domain.Models.Message
import com.ncs.o2.Domain.Models.ServerResult
import com.ncs.o2.Domain.Models.UserInMessage
import com.ncs.o2.Domain.Repositories.FirestoreRepository
import com.ncs.o2.Domain.Utility.ExtensionsUtil.gone
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickThrottleBounceListener
import com.ncs.o2.Domain.Utility.ExtensionsUtil.visible
import com.ncs.o2.Domain.Utility.DateTimeUtils
import com.ncs.o2.Domain.Utility.ExtensionsUtil.loadProfileImg
import com.ncs.o2.databinding.ChatMessageItemBinding
import com.ncs.versa.Constants.Endpoints
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

class ChatAdapter(val repository: FirestoreRepository, var msgList: MutableList<Message>, val context : Context,private val onchatDoubleClickListner: onChatDoubleClickListner) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var messageDatabase: MessageDatabase
    var db: UsersDao
    var users: MutableList<UserInMessage> = mutableListOf()
    private var lastTimestamp: Date? = null


    init {
        messageDatabase = Room.databaseBuilder(
            context,
            MessageDatabase::class.java,
            Endpoints.ROOM.MESSAGES.USERLIST_DB
        ).build()
        db = messageDatabase.usersDao()
        msgList.sortBy { it.timestamp?.toDate() }

        msgList.sortBy { it.timestamp!!.seconds }

    }

    companion object {
        const val NORMAL_MSG = 0
        const val IMAGE_MSG = 1
        const val FILE_MSG = 2
    }

    private inner class UserMessage_ViewHolder(val binding: ChatMessageItemBinding) :
        RecyclerView.ViewHolder(binding.root) {


        fun bind(position: Int) {
            val senderId = msgList[position].senderId
            val localUser = users.find { it.EMAIL == senderId }

            if (localUser != null) {
                setChatItem(localUser, binding, position)
                Log.d("DB", "fetching from local")
            } else {
                fetchUser(senderId) { newUser ->
                    users.add(newUser)
                    Log.d("DB", "fetching from db")
                    setChatItem(newUser, binding, position)
                }
            }

            var doubleClick: Boolean? = false
            binding.root.setOnClickThrottleBounceListener {
                if (doubleClick!!) {
                    if (localUser!=null){
                        onchatDoubleClickListner.onDoubleClickListner(msgList[position],localUser.USERNAME!!)
                    }
                    else{
                        fetchUser(senderId){
                            onchatDoubleClickListner.onDoubleClickListner(msgList[position],it.USERNAME!!)
                        }
                    }
                }
                doubleClick = true
                Handler().postDelayed({ doubleClick = false }, 2000)
            }
        }
    }



        fun setChatItem(user: UserInMessage, binding: ChatMessageItemBinding, position: Int) {
            val msg = msgList.get(position).content
            setMessageView(msgList[position], binding)
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
            val time = msgList.get(position).timestamp!!
            binding.tvTimestamp.text = DateTimeUtils.getTimeAgo(time.seconds)
            binding.imgDp.loadProfileImg(user.DP_URL.toString())
            binding.tvName.text = user.USERNAME
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return when (viewType) {

                NORMAL_MSG -> {
                    UserMessage_ViewHolder(
                        ChatMessageItemBinding.inflate(
                            LayoutInflater.from(context),
                            parent,
                            false
                        )
                    )
                }

                else -> {
                    throw IllegalArgumentException("Invalid view type")
                }
            }
        }

        private fun setMessageView(message: Message, binding: ChatMessageItemBinding) {

            val css: InternalStyleSheet = Github()

            with(css) {
                addFontFace(
                    "o2font",
                    "normal",
                    "normal",
                    "normal",
                    "url('file:///android_res/font/sfregular.ttf')"
                )
                addRule("body", "font-family:o2font")
                addRule("body", "font-size:16px")
                addRule("body", "line-height:21px")
                addRule("body", "background-color: #131313")
                addRule("body", "color: #fff")
                addRule("body", "padding: 0px 0px 0px 0px")
                addRule("a", "color: #86ff7c")
                addRule("pre", "border: 1px solid #000;")
                addRule("pre", "border-radius: 4px;")
                addRule("pre", "max-height: 400px;")
                addRule("pre", "overflow:auto")
                addRule("pre", "white-space: pre-line")

            }

            binding.markdownView.settings.javaScriptEnabled = true
            binding.markdownView.addStyleSheet(css)

            binding.markdownView.webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {

                }

                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {
                    val intent = Intent(Intent.ACTION_VIEW, request?.url)
                    context.startActivity(intent)
                    return true
                }

            }
            binding.markdownView.loadMarkdown(message.content)
            binding.markdownView.visible()


        }

        override fun getItemCount(): Int {
            return msgList.size
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

            (holder as UserMessage_ViewHolder).bind(position)
        }

    fun appendMessages(newMessages: List<Message>) {
        val uniqueNewMessages = newMessages.filter { message ->
            !msgList.any { it.messageId == message.messageId }
        }

        if (uniqueNewMessages.isNotEmpty()) {
            msgList.addAll(uniqueNewMessages)
            msgList.sortBy { it.timestamp!!.seconds }

            val startPosition = msgList.size - uniqueNewMessages.size
            notifyItemRangeInserted(startPosition, uniqueNewMessages.size)
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

                    is ServerResult.Progress -> {

                    }
                }
            }
    }
    interface onChatDoubleClickListner{
        fun onDoubleClickListner(msg: Message,senderName:String)
    }
}