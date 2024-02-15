package com.ncs.o2.UI.Tasks.TaskPage.Chat.Adapters

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat.startActivity
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
import com.ncs.o2.Domain.Utility.ExtensionsUtil.isNull
import com.ncs.o2.Domain.Utility.ExtensionsUtil.load
import com.ncs.o2.Domain.Utility.ExtensionsUtil.loadProfileImg
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickThrottleBounceListener
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnDoubleClickListener
import com.ncs.o2.Domain.Utility.ExtensionsUtil.visible
import com.ncs.o2.R
import com.ncs.o2.UI.Tasks.TaskPage.Details.ImageViewerActivity
import com.ncs.o2.UI.Tasks.TaskPage.TaskDetailActivity
import com.ncs.o2.databinding.ChatImageItemBinding
import com.ncs.o2.databinding.ChatMessageItemBinding
import com.ncs.o2.databinding.ChatMessageLinkItemBinding
import com.ncs.o2.databinding.ChatMessageReplyItemBinding
import com.ncs.versa.Constants.Endpoints
import io.noties.markwon.Markwon
import timber.log.Timber
import java.util.Date
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern


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
    val activitybinding:TaskDetailActivity,
    val repository: FirestoreRepository,
    var msgList: MutableList<Message>,
    val context: Context,
    val moderatorList : MutableList<String>,
    val assignee : String,
    private val onchatDoubleClickListner: onChatDoubleClickListner,
    private val markwon: Markwon,
    private val onMessageLongPress: OnMessageLongPress
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
        const val REPLY_MSG = 2
        const val LINK_MSG=3
        const val FILE_MSG = 4
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

            binding.parentMessageItem.setOnLongClickListener {
                if (localUser != null) {
                    onMessageLongPress.onLongPress(msgList[position],localUser.USERNAME!!)
                    true

                } else {
                    fetchUser(senderId) {
                        onMessageLongPress.onLongPress(msgList[position],it.USERNAME!!)
                    }
                    true
                }

            }

            binding.descriptionTv.setOnLongClickListener {
                if (localUser != null) {
                    onMessageLongPress.onLongPress(msgList[position],localUser.USERNAME!!)
                    true

                } else {
                    fetchUser(senderId) {
                        onMessageLongPress.onLongPress(msgList[position],it.USERNAME!!)
                    }
                    true
                }

            }

            binding.parentMessageItem.setOnDoubleClickListener {

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

    private inner class UserMessage_Reply_ViewHolder(val binding: ChatMessageReplyItemBinding) :
        RecyclerView.ViewHolder(binding.root) {


        fun bind(position: Int) {

            val senderId = msgList[position].senderId
            val localUser = users.find { it.EMAIL == senderId }

            val replyingToMessageID : String = msgList[position].additionalData?.get("replyingTo").toString()
            val replyingToMessage = msgList.find { it.messageId == replyingToMessageID }

            binding.referenceToUsername.text = replyingToMessage?.senderId
            binding.referenceText.text = replyingToMessage?.content


            if (localUser != null) {
                setChatReplyItem(localUser, binding, position)
                Timber.tag("DB").d("fetching from local")

            } else {
                fetchUser(senderId) { newUser ->
                    users.add(newUser)
                    Timber.tag("DB").d("fetching from db")
                    setChatReplyItem(newUser, binding, position)
                }
            }

            binding.parentMessageItem.setOnLongClickListener {
                if (localUser != null) {
                    onMessageLongPress.onLongPress(msgList[position],localUser.USERNAME!!)
                    true

                } else {
                    fetchUser(senderId) {
                        onMessageLongPress.onLongPress(msgList[position],it.USERNAME!!)
                    }
                    true
                }

            }

            binding.descriptionTv.setOnLongClickListener {
                if (localUser != null) {
                    onMessageLongPress.onLongPress(msgList[position],localUser.USERNAME!!)
                    true

                } else {
                    fetchUser(senderId) {
                        onMessageLongPress.onLongPress(msgList[position],it.USERNAME!!)
                    }
                    true
                }

            }

            binding.parentMessageItem.setOnDoubleClickListener {

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

    private inner class UserMessage_Link_ViewHolder(val binding: ChatMessageLinkItemBinding) :
        RecyclerView.ViewHolder(binding.root) {


        fun bind(position: Int) {

            val senderId = msgList[position].senderId
            val localUser = users.find { it.EMAIL == senderId }

            if (localUser != null) {
                setChatLinkItem(localUser, binding, position)
                Timber.tag("DB").d("fetching from local")

            } else {
                fetchUser(senderId) { newUser ->
                    users.add(newUser)
                    Timber.tag("DB").d("fetching from db")
                    setChatLinkItem(newUser, binding, position)
                }
            }

            val title=msgList[position].additionalData?.getValue("Title")
            val desc=msgList[position].additionalData?.getValue("Open Graph Description")
            val image=msgList[position].additionalData?.getValue("Open Graph Image")
            val url=msgList[position].additionalData?.getValue("Url")
            val type=msgList[position].additionalData?.getValue("Type")


            if (!title.isNull){
                binding.linkPreviewTitle.text=title.toString()
            }
            if (!type.isNull){
                if (type=="normal") {
                    if (!desc.isNull){
                        binding.linkPreviewDesc.text=desc.toString()
                    }
                }
                else{
                    binding.linkPreviewDesc.text=if (!url.isNull) url.toString() else ""
                }
            }

            if (!type.isNull){
                if (type=="normal") {
                    if (!image.isNull) {
                        binding.linkPreviewImage.load(
                            url = image.toString(),
                            placeholder = context.resources.getDrawable(R.drawable.placeholder_image)
                        )
                    }
                }
                else{
                    binding.linkPreviewImage.setImageDrawable(context.resources.getDrawable(R.drawable.apphd))
                }
            }
            if (!url.isNull) {
                binding.linkPreview.setOnClickThrottleBounceListener {
                    openInBrowser(url.toString())
                }
            }

            binding.parentMessageItem.setOnLongClickListener {
                if (localUser != null) {
                    onMessageLongPress.onLongPress(msgList[position],localUser.USERNAME!!)
                    true

                } else {
                    fetchUser(senderId) {
                        onMessageLongPress.onLongPress(msgList[position],it.USERNAME!!)
                    }
                    true
                }

            }

            binding.descriptionTv.setOnLongClickListener {
                if (localUser != null) {
                    onMessageLongPress.onLongPress(msgList[position],localUser.USERNAME!!)
                    true

                } else {
                    fetchUser(senderId) {
                        onMessageLongPress.onLongPress(msgList[position],it.USERNAME!!)
                    }
                    true
                }

            }

            binding.parentMessageItem.setOnDoubleClickListener {

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

    private fun openInBrowser(url: String) {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(browserIntent)
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
        binding.tvName.text = user.USERNAME
        binding.imagePreview.load(url,R.drawable.placeholder_image,"")
        binding.imagePreview.visible()
        binding.imageProgressBar.gone()
        binding.imagePreview.setOnClickThrottleBounceListener {
//            OnImageClicked.onImageClick(0, listOf(url))
            onImageClick(0, listOf(url))
        }
        binding.parent.setOnClickThrottleBounceListener {
//            OnImageClicked.onImageClick(0, listOf(url))
            onImageClick(0, listOf(url))
        }

        setImgMsgDPHeader(position, binding, user)

    }

    fun setChatItem(user: UserInMessage, binding: ChatMessageItemBinding, position: Int) {
        setMessageView(msgList[position].content,binding)
        val time = msgList[position].timestamp!!
        binding.tvTimestamp.text = DateTimeUtils.getTimeAgo(time.seconds)
        binding.tvName.text = user.USERNAME
        setNormalMsgDPHeader(position, binding, user)
    }

    fun setChatReplyItem(user: UserInMessage, binding: ChatMessageReplyItemBinding, position: Int) {
        setMessageReplyView(msgList[position], binding)
        val time = msgList[position].timestamp!!
        binding.tvTimestamp.text = DateTimeUtils.getTimeAgo(time.seconds)
        binding.tvName.text = user.USERNAME
        setReplyDPHeader(position, binding, user)
    }

    fun setChatLinkItem(user: UserInMessage, binding: ChatMessageLinkItemBinding, position: Int) {
        setMessageLinkView(msgList[position], binding)
        val time = msgList[position].timestamp!!
        binding.tvTimestamp.text = DateTimeUtils.getTimeAgo(time.seconds)
        binding.tvName.text = user.USERNAME
        setLinkDPHeader(position, binding, user)
    }

    private fun setImgMsgDPHeader(position: Int, binding: ChatImageItemBinding, user: UserInMessage) {


        // No changes for the first item
        if (position == 0) {
            binding.msgSeperator.gone()
            binding.imgDp.visible()
            binding.tvName.visible()
            binding.tvTimestamp.gravity = Gravity.END or Gravity.CENTER
            binding.imgDp.loadProfileImg(user.DP_URL.toString())


            if (moderatorList.contains(user.EMAIL)){
                binding.modTag.visible()
                binding.tvName.setTextColor(context.resources.getColor(R.color.light_blue_A200))

            }else {
                binding.modTag.gone()
                binding.tvName.setTextColor(context.resources.getColor(R.color.primary))

            }

            if (assignee == user.EMAIL){
                binding.assigneeTag.visible()
            }else{
                binding.assigneeTag.gone()
            }

            return
        }

        // Removing dp and changing some layout if the previous message is sent from same user
        val timestamp1 = msgList[position].timestamp?.toDate()?.time
        val timestamp2 = msgList[position - 1].timestamp?.toDate()?.time
        val timeDifferenceMillis = timestamp1?.minus(timestamp2!!)
        val timeDifferenceMinutes = timeDifferenceMillis?.let { TimeUnit.MILLISECONDS.toMinutes(it) }

        if (msgList[position - 1].senderId == msgList[position].senderId && timeDifferenceMinutes!! < 10) {
            binding.imgDp.loadProfileImg(R.drawable.baseline_subdirectory_arrow_right_24)
            binding.tvName.gone()
            binding.tvTimestamp.gravity = Gravity.START or Gravity.CENTER
            binding.msgSeperator.gone()

            binding.modTag.gone()
            binding.assigneeTag.gone()
            return

        }

        // All the other items

        binding.msgSeperator.visible()
        binding.imgDp.visible()
        binding.tvName.visible()
        binding.tvTimestamp.gravity = Gravity.END or Gravity.CENTER
        binding.msgSeperator.alpha = 1f
        binding.imgDp.loadProfileImg(user.DP_URL.toString())

        if (moderatorList.contains(user.EMAIL)){
            binding.modTag.visible()
            binding.tvName.setTextColor(context.resources.getColor(R.color.light_blue_A200))

        }else {
            binding.modTag.gone()
            binding.tvName.setTextColor(context.resources.getColor(R.color.primary))

        }

        if (assignee == user.EMAIL){
            binding.assigneeTag.visible()
        }else{
            binding.assigneeTag.gone()
        }

    }

    private fun setNormalMsgDPHeader(position: Int, binding: ChatMessageItemBinding, user: UserInMessage) {


        // No changes for the first item
        if (position == 0) {
            binding.msgSeperator.gone()
            binding.imgDp.visible()
            binding.tvName.visible()
            binding.tvTimestamp.gravity = Gravity.END or Gravity.CENTER
            binding.imgDp.loadProfileImg(user.DP_URL.toString())

            if (moderatorList.contains(user.EMAIL)){
                binding.modTag.visible()
                binding.tvName.setTextColor(context.resources.getColor(R.color.light_blue_A200))

            }else {
                binding.modTag.gone()
                binding.tvName.setTextColor(context.resources.getColor(R.color.primary))
            }

            if (assignee == user.EMAIL){
                binding.assigneeTag.visible()
            }else{
                binding.assigneeTag.gone()
            }

            return
        }

        // Removing dp and changing some layout if the previous message is sent from same user
        val timestamp1 = msgList[position].timestamp?.toDate()?.time
        val timestamp2 = msgList[position - 1].timestamp?.toDate()?.time
        val timeDifferenceMillis = timestamp1?.minus(timestamp2!!)
        val timeDifferenceMinutes = timeDifferenceMillis?.let { TimeUnit.MILLISECONDS.toMinutes(it) }
        if (msgList[position - 1].senderId == msgList[position].senderId && timeDifferenceMinutes!! < 10) {
            binding.imgDp.loadProfileImg(R.drawable.baseline_subdirectory_arrow_right_24)
            binding.tvName.gone()
            binding.tvTimestamp.gravity = Gravity.START or Gravity.CENTER
            binding.msgSeperator.gone()
            binding.modTag.gone()
            binding.assigneeTag.gone()
            return
        }

        // All the other items
        binding.msgSeperator.visible()
        binding.imgDp.visible()
        binding.tvName.visible()
        binding.tvTimestamp.gravity = Gravity.END or Gravity.CENTER
        binding.msgSeperator.alpha = 1f
        binding.imgDp.loadProfileImg(user.DP_URL.toString())

        if (moderatorList.contains(user.EMAIL)){
            binding.modTag.visible()
            binding.tvName.setTextColor(context.resources.getColor(R.color.light_blue_A200))
        }else {
            binding.modTag.gone()
            binding.tvName.setTextColor(context.resources.getColor(R.color.primary))

        }

        if (assignee == user.EMAIL){
            binding.assigneeTag.visible()
        }else{
            binding.assigneeTag.gone()
        }
    }

    private fun setReplyDPHeader(position: Int, binding: ChatMessageReplyItemBinding, user: UserInMessage) {

        // Removing dp and changing some layout if the previous message is sent from same user
        val timestamp1 = msgList[position].timestamp?.toDate()?.time
        val timestamp2 = msgList[position - 1].timestamp?.toDate()?.time
        val timeDifferenceMillis = timestamp1?.minus(timestamp2!!)
        val timeDifferenceMinutes = timeDifferenceMillis?.let { TimeUnit.MILLISECONDS.toMinutes(it) }

        if (msgList[position - 1].senderId == msgList[position].senderId && (timeDifferenceMinutes!! < 10)) {
            binding.imgDp.loadProfileImg(R.drawable.baseline_subdirectory_arrow_right_24)
            binding.tvName.gone()
            binding.tvTimestamp.gravity = Gravity.START or Gravity.CENTER
            binding.msgSeperator.gone()
            binding.modTag.gone()
            binding.assigneeTag.gone()
            return
        }

        // All the other items
        binding.msgSeperator.visible()
        binding.imgDp.visible()
        binding.tvName.visible()
        binding.tvTimestamp.gravity = Gravity.END or Gravity.CENTER
        binding.msgSeperator.alpha = 1f
        binding.imgDp.loadProfileImg(user.DP_URL.toString())

        if (moderatorList.contains(user.EMAIL)){
            binding.modTag.visible()
            binding.tvName.setTextColor(context.resources.getColor(R.color.light_blue_A200))
        }else {
            binding.modTag.gone()
            binding.tvName.setTextColor(context.resources.getColor(R.color.primary))

        }

        if (assignee == user.EMAIL){
            binding.assigneeTag.visible()
        }else{
            binding.assigneeTag.gone()
        }
    }

    private fun setLinkDPHeader(position: Int, binding: ChatMessageLinkItemBinding, user: UserInMessage) {

        if (position == 0) {
            binding.msgSeperator.gone()
            binding.imgDp.visible()
            binding.tvName.visible()
            binding.tvTimestamp.gravity = Gravity.END or Gravity.CENTER
            binding.imgDp.loadProfileImg(user.DP_URL.toString())

            if (moderatorList.contains(user.EMAIL)){
                binding.modTag.visible()
                binding.tvName.setTextColor(context.resources.getColor(R.color.light_blue_A200))

            }else {
                binding.modTag.gone()
                binding.tvName.setTextColor(context.resources.getColor(R.color.primary))
            }

            if (assignee == user.EMAIL){
                binding.assigneeTag.visible()
            }else{
                binding.assigneeTag.gone()
            }

            return
        }

        // Removing dp and changing some layout if the previous message is sent from same user
        val timestamp1 = msgList[position].timestamp?.toDate()?.time
        val timestamp2 = msgList[position - 1].timestamp?.toDate()?.time
        val timeDifferenceMillis = timestamp1?.minus(timestamp2!!)
        val timeDifferenceMinutes = timeDifferenceMillis?.let { TimeUnit.MILLISECONDS.toMinutes(it) }
        if (msgList[position - 1].senderId == msgList[position].senderId && timeDifferenceMinutes!! < 10) {
            binding.imgDp.loadProfileImg(R.drawable.baseline_subdirectory_arrow_right_24)
            binding.tvName.gone()
            binding.tvTimestamp.gravity = Gravity.START or Gravity.CENTER
            binding.msgSeperator.gone()
            binding.modTag.gone()
            binding.assigneeTag.gone()
            return
        }

        // All the other items
        binding.msgSeperator.visible()
        binding.imgDp.visible()
        binding.tvName.visible()
        binding.tvTimestamp.gravity = Gravity.END or Gravity.CENTER
        binding.msgSeperator.alpha = 1f
        binding.imgDp.loadProfileImg(user.DP_URL.toString())

        if (moderatorList.contains(user.EMAIL)){
            binding.modTag.visible()
            binding.tvName.setTextColor(context.resources.getColor(R.color.light_blue_A200))
        }else {
            binding.modTag.gone()
            binding.tvName.setTextColor(context.resources.getColor(R.color.primary))

        }

        if (assignee == user.EMAIL){
            binding.assigneeTag.visible()
        }else{
            binding.assigneeTag.gone()
        }
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

            REPLY_MSG -> {
                UserMessage_Reply_ViewHolder(
                    ChatMessageReplyItemBinding.inflate(
                        LayoutInflater.from(context),
                        parent,
                        false
                    )
                )
            }

            LINK_MSG-> {
                UserMessage_Link_ViewHolder(
                    ChatMessageLinkItemBinding.inflate(
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

    private fun processSpan(message: Spanned) : SpannableStringBuilder{
        val spannable = SpannableStringBuilder(message)
        val mentionedUsers: MutableList<String> = mutableListOf()

        val mentionPattern = Pattern.compile("@(\\w+)")
        val mentionMatcher = mentionPattern.matcher(message)

        while (mentionMatcher.find()) {
            val user = mentionMatcher.group(1)
            mentionedUsers.add(user)
            val startIndex = mentionMatcher.start()
            val endIndex = message.indexOf(" ", startIndex).takeIf { it != -1 } ?: mentionMatcher.end()

            spannable.setSpan(
                StyleSpan(Typeface.BOLD),
                startIndex,
                endIndex,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            spannable.setSpan(
                ForegroundColorSpan(context.resources.getColor(R.color.primary)),
                startIndex,
                endIndex,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )

        }
        return spannable
    }

    private fun setMessageView(message: String, binding: ChatMessageItemBinding) {
        val links=convertLinksToHtml(message)
        val spannedMessage = processSpan(markwon.render(markwon.parse(links)))
        markwon.setParsedMarkdown(binding.descriptionTv, spannedMessage)
        binding.descriptionTv.visible()
    }

    fun convertLinksToHtml(text: String): String {
        val pattern = "([\\w+]+\\:\\/\\/)?([\\w\\d-]+\\.)*[\\w-]+[\\.\\:]\\w+([\\/\\?\\=\\&\\#\\.]?[\\w-]+)*\\/?".toRegex()
        val replacedText = pattern.replace(text) { matchResult ->
            val url = matchResult.value
            if (url.startsWith("www.") || url.startsWith("http")) {
                """<a href="$url" target="_blank">$url</a>"""
            } else {
                """<a href="http://$url" target="_blank">$url</a>"""
            }
        }
        return replacedText
    }


    private fun setMessageReplyView(message: Message, binding: ChatMessageReplyItemBinding) {
        val links=convertLinksToHtml(message.content)
        val spannedMessage = processSpan(markwon.render(markwon.parse(links)))
        markwon.setParsedMarkdown(binding.descriptionTv, spannedMessage)
        binding.descriptionTv.visible()

    }

    private fun setMessageLinkView(message: Message, binding: ChatMessageLinkItemBinding) {
        val links=convertLinksToHtml(message.content)
        val spannedMessage = processSpan(markwon.render(markwon.parse(links)))
        markwon.setParsedMarkdown(binding.descriptionTv, spannedMessage)
        binding.descriptionTv.visible()

    }


    override fun getItemCount(): Int {
        return msgList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (msgList[position].messageType == MessageType.NORMAL_MSG) {
            (holder as UserMessage_ViewHolder).bind(position)
        }
        else if (msgList[position].messageType == MessageType.REPLY_MSG){
            (holder as UserMessage_Reply_ViewHolder).bind(position)
        }
        else if (msgList[position].messageType == MessageType.LINK_MSG){
            (holder as UserMessage_Link_ViewHolder).bind(position)
        }
        else if (msgList[position].messageType == MessageType.IMAGE_MSG) {
            (holder as ImageMessage_ViewHolder).bind(position)
        }

    }

    override fun getItemViewType(position: Int): Int{
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
                        activitybinding.sharedViewModel.pushReceiver(user.FCM_TOKEN!!)
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
    interface OnMessageLongPress{
        fun onLongPress(message: Message,senderName: String)
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