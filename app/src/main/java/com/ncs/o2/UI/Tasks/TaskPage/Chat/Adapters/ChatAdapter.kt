package com.ncs.o2.UI.Tasks.TaskPage.Chat.Adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.ncs.o2.Data.Room.MessageRepository.MessageDatabase
import com.ncs.o2.Data.Room.MessageRepository.UsersDao
import com.ncs.o2.Domain.Models.Message
import com.ncs.o2.Domain.Models.ServerResult
import com.ncs.o2.Domain.Models.UserInMessage
import com.ncs.o2.Domain.Repositories.FirestoreRepository
import com.ncs.o2.Domain.Utility.DateTimeUtils
import com.ncs.o2.Domain.Utility.ExtensionsUtil.loadProfileImg
import com.ncs.o2.databinding.ChatMessageItemBinding
import com.ncs.versa.Constants.Endpoints


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
    val context: Context
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var messageDatabase: MessageDatabase = Room.databaseBuilder(
        context,
        MessageDatabase::class.java,
        Endpoints.ROOM.MESSAGES.USERLIST_DB
    ).build()

    var db: UsersDao = messageDatabase.usersDao()
    var users: MutableList<UserInMessage> = mutableListOf()

    init {
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
        }
    }

    fun setChatItem(user: UserInMessage, binding: ChatMessageItemBinding, position: Int) {
        val msg = msgList.get(position).content
        val time = msgList.get(position).timestamp!!

        binding.tvMessage.text = msg
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

    override fun getItemCount(): Int {
        return msgList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        (holder as UserMessage_ViewHolder).bind(position)
    }

    public fun appendMessage(msg: Message) {
        msgList.add(msg)
        notifyDataSetChanged()
    }

//    public fun showTyping(show:Boolean){
//       if (show){
//           msgList.add(Message("123","123","Typing...", Timestamp.now(), MessageType.NORMAL_MSG, emptyMap()))
//           notifyDataSetChanged()
//       }else{
//           msgList.removeAt(msgList.size-1)
//           notifyDataSetChanged()
//       }
//    }

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

}