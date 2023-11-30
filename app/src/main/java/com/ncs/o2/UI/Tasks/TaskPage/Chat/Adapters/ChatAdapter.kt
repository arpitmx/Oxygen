package com.ncs.o2.UI.Tasks.TaskPage.Chat.Adapters

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.ncs.o2.Data.Room.MessageRepository.MessageDatabase
import com.ncs.o2.Data.Room.MessageRepository.UsersDao
import com.ncs.o2.Domain.Models.Enums.MessageType
import com.ncs.o2.Domain.Models.Message
import com.ncs.o2.Domain.Models.ServerResult
import com.ncs.o2.Domain.Models.UserInMessage
import com.ncs.o2.Domain.Repositories.FirestoreRepository
import com.ncs.o2.Domain.Utility.DateTimeUtils
import com.ncs.o2.Domain.Utility.ExtensionsUtil.gone
import com.ncs.o2.Domain.Utility.ExtensionsUtil.loadProfileImg
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickThrottleBounceListener
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnDoubleClickListener
import com.ncs.o2.Domain.Utility.ExtensionsUtil.visible
import com.ncs.o2.R
import com.ncs.o2.UI.Tasks.TaskPage.Details.ImageViewerActivity
import com.ncs.o2.databinding.ChatImageItemBinding
import com.ncs.o2.databinding.ChatMessageItemBinding
import com.ncs.versa.Constants.Endpoints
import io.noties.markwon.Markwon
import timber.log.Timber
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

class ChatAdapter(
    val repository: FirestoreRepository,
    var msgList: MutableList<Message>,
    val context: Context,
    private val onchatDoubleClickListner: onChatDoubleClickListner,
    private val markwon: Markwon
) :
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

            binding.descriptionTv.setOnDoubleClickListener {

                if (localUser != null) {
                    onchatDoubleClickListner.onDoubleClickListner(
                        msgList[position],
                        localUser.USERNAME!!
                    )
                } else {
                    fetchUser(senderId) {
                        onchatDoubleClickListner.onDoubleClickListner(
                            msgList[position],
                            it.USERNAME!!
                        )
                    }
                }

            }
        }
    }


    private inner class ImageMessage_ViewHolder(val binding: ChatImageItemBinding) :
        RecyclerView.ViewHolder(binding.root) {


        fun bind(position: Int) {
            val senderId = msgList[position].senderId
            val localUser = users.find { it.EMAIL == senderId }

            if (localUser != null) {
                setImageItem(localUser, binding, position)
                Timber.tag("DB").d("fetching from local")
            } else {
                fetchUser(senderId) { newUser ->
                    users.add(newUser)
                    Timber.tag("DB").d("fetching from db")
                    setImageItem(newUser, binding, position)
                }
            }
        }

        }


    fun setImageItem(user: UserInMessage, binding: ChatImageItemBinding, position: Int) {
        val url = msgList.get(position).content
        val time = msgList[position].timestamp!!
        binding.tvTimestamp.text = DateTimeUtils.getTimeAgo(time.seconds)
        binding.imgDp.loadProfileImg(user.DP_URL.toString())
        binding.tvName.text = user.USERNAME
        binding.imagePreview.loadProfileImg(url)


        binding.imagePreview.setOnClickThrottleBounceListener {
//            OnImageClicked.onImageClick(0, listOf(url))
            onImageClick(0, listOf(url))
        }
        binding.parent.setOnClickThrottleBounceListener {
//            OnImageClicked.onImageClick(0, listOf(url))
            onImageClick(0, listOf(url))
        }
    }

    fun setChatItem(user: UserInMessage, binding: ChatMessageItemBinding, position: Int) {
        setMessageView(msgList[position], binding)
        val time = msgList[position].timestamp!!
        binding.tvTimestamp.text = DateTimeUtils.getTimeAgo(time.seconds)
        binding.tvName.text = user.USERNAME

        setDP(position, binding, user)


    }

    private fun setDP(position: Int, binding: ChatMessageItemBinding, user: UserInMessage) {

        // No changes for the first item
        if (position == 0) {
            binding.msgSeperator.gone()
            binding.imgDp.visible()
            binding.tvName.visible()
            binding.tvTimestamp.gravity = Gravity.END or Gravity.CENTER
            binding.imgDp.loadProfileImg(user.DP_URL.toString())
            return
        }

        // Removing dp and changing some layout if the previous message is sent from same user
        if (msgList[position - 1].senderId == msgList[position].senderId) {
            binding.imgDp.loadProfileImg(R.drawable.baseline_subdirectory_arrow_right_24)
            binding.tvName.gone()
            binding.tvTimestamp.gravity = Gravity.START or Gravity.CENTER
            binding.msgSeperator.alpha = 0.5f

            return
        }

        // All the other items
        binding.msgSeperator.visible()
        binding.imgDp.visible()
        binding.tvName.visible()
        binding.tvTimestamp.gravity = Gravity.END or Gravity.CENTER
        binding.msgSeperator.alpha = 1f
        binding.imgDp.loadProfileImg(user.DP_URL.toString())


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

            IMAGE_MSG -> {
                ImageMessage_ViewHolder(
                    ChatImageItemBinding.inflate(
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

        markwon.setMarkdown(binding.descriptionTv, message.content)
        binding.descriptionTv.visible()

    }

    override fun getItemCount(): Int {
        return msgList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (msgList[position].messageType == MessageType.NORMAL_MSG) {
            (holder as UserMessage_ViewHolder).bind(position)
        } else if (msgList[position].messageType == MessageType.IMAGE_MSG) {
            (holder as ImageMessage_ViewHolder).bind(position)
        }

    }

    override fun getItemViewType(position: Int): Int {
        return msgList[position].messageType.ordinal
    }

    fun appendMessages(newMessages: List<Message>) {
        val uniqueNewMessages = newMessages.filter { message ->
            !msgList.any { it.messageId == message.messageId }
        }

        Timber.tag("ChatAdapter").d("New messages : $newMessages")

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

    interface onChatDoubleClickListner {
        fun onDoubleClickListner(msg: Message, senderName: String)
    }
    interface onImageClicked{
        fun onImageClick(position: Int,imageUrls: List<String>)
    }
    fun onImageClick(position: Int, imageUrls: List<String>) {
        val imageViewerIntent = Intent(context, ImageViewerActivity::class.java)
        imageViewerIntent.putExtra("position", position)
        imageViewerIntent.putStringArrayListExtra("images", ArrayList(imageUrls))
        context.startActivity(
            ImageViewerActivity.createIntent(
                context,
                ArrayList(imageUrls),
                position,
            ),
        )
    }
}