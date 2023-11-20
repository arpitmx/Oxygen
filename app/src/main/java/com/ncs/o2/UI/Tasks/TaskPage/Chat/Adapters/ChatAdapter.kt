package com.ncs.o2.UI.Tasks.TaskPage.Chat.Adapters

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ncs.o2.Domain.Models.Message
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickThrottleBounceListener
import io.noties.markwon.Markwon


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

//class ChatAdapter(var msgList: MutableList<Message>, val context : Context, val markwon: Markwon, private val onClickCallback: OnClickCallback
//, private val openPDFcallback:PdfHandlerCallback
//) :
//    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
//
//    interface onMessageClick {
//        fun onClick(position: Int)
//
//    }
//
//    interface onRecentAppClick {
//        fun onRecentClick(position: Int)
//    }
//
//    companion object {
//        const val MESSAGE_TYPE_BOT = 0
//        const val MESSAGE_TYPE_USER = 1
//        const val MESSAGE_TYPE_BOT_PDF = 2
//        const val MESSAGE_TYPE_BOT_TYPING = 3
//    }
//
//
//    private inner class BotMessage_ViewHolder( val binding : BotMessageItemBinding) :
//        RecyclerView.ViewHolder(binding.root){
//
//        fun bind(position: Int){
//            val msg = msgList.get(position).message
//            //binding.tvMessage.setText(msg)
//            markwon.setMarkdown(binding.tvMessage, msg)
//
//            binding.btnCopy.setOnClickThrottleBounceListener {
//                onClickCallback.copyClick(msg)
//            }
//
//            binding.tvMessage.setOnLongClickListener{
//                val msgObj = Message(msg, MESSAGE_TYPE_BOT)
//                onClickCallback.onLongClick(msgObj)
//                true
//            }
//        }
//    }
//
//
//
//    private inner class BotMessage_PDF_ViewHolder( val binding : BotMessagePdfItemBinding) :
//        RecyclerView.ViewHolder(binding.root){
//
//        fun bind(position: Int){
//
//            val msg : PdfMessage = msgList.get(position) as PdfMessage
//            binding.pdfName.text = msg.fileName
//            binding.pdfThumb.setImageBitmap(msg.bitmap)
//            binding.btnOpenPdf.setOnClickThrottleBounceListener(600){
//                openPDFcallback.openPdf(msg.uri)
//            }
//
//            binding.btnSendPdf.setOnClickThrottleBounceListener(600){
//                openPDFcallback.sendPdf(msg.uri)
//            }
//        }
//
//    }
//
//    private inner class BotMessage_Typing_ViewHolder( val binding : BotMessageTypingItemBinding) :
//        RecyclerView.ViewHolder(binding.root){
//
//        fun bind(position: Int){
//        }
//
//    }
//
//
//    private inner class UserMessage_ViewHolder( val binding: UserMessageItemBinding) :
//        RecyclerView.ViewHolder(binding.root){
//        fun bind(position: Int){
//            val msg = msgList.get(position).message
//            binding.tvMessage.setText(msg)
//        }
//    }
//
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
//        return when (viewType) {
//            MESSAGE_TYPE_BOT -> {
//                BotMessage_ViewHolder(BotMessageItemBinding.inflate(LayoutInflater.from(context),parent,false))
//
//            }
//            MESSAGE_TYPE_USER -> {
//                UserMessage_ViewHolder(UserMessageItemBinding.inflate(LayoutInflater.from(context),parent,false))
//            }
//
//            MESSAGE_TYPE_BOT_PDF -> {
//                BotMessage_PDF_ViewHolder(BotMessagePdfItemBinding.inflate(LayoutInflater.from(context),parent,false))
//            }
//
//            MESSAGE_TYPE_BOT_TYPING -> {
//                BotMessage_Typing_ViewHolder(BotMessageTypingItemBinding.inflate(LayoutInflater.from(context),parent,false))
//            }
//
//            else -> {
//                throw IllegalArgumentException("Invalid view type")
//            }
//        }
//    }
//
//    override fun getItemCount(): Int {
//        return msgList.size
//    }
//
//    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
//        if (msgList[position].msgType == MESSAGE_TYPE_BOT) {
//            (holder as BotMessage_ViewHolder).bind(position)
//
//        } else if (msgList[position].msgType == MESSAGE_TYPE_USER) {
//            (holder as UserMessage_ViewHolder).bind(position)
//        }
//        else if (msgList[position].msgType == MESSAGE_TYPE_BOT_PDF) {
//            (holder as BotMessage_PDF_ViewHolder).bind(position)
//        }
//        else if (msgList[position].msgType == MESSAGE_TYPE_BOT_TYPING) {
//            (holder as BotMessage_Typing_ViewHolder).bind(position)
//        }
//    }
//
//    override fun getItemViewType(position: Int): Int {
//        return msgList[position].msgType
//    }
//
//    public fun appendMessage(msg:Message){
//        msgList.add(msg)
//        notifyDataSetChanged()
//    }
//
//    public fun showTyping(show:Boolean){
//       if (show){
//           msgList.add(Message("Typing",3))
//           notifyDataSetChanged()
//       }else{
//           msgList.removeAt(msgList.size-1)
//           notifyDataSetChanged()
//       }
//    }
//
//
//
//    interface OnClickCallback{
//        fun onLongClick(msg: Message)
//        fun copyClick(msg:String)
//    }
//
//    interface PdfHandlerCallback{
//        fun openPdf(uri: Uri)
//        fun sendPdf(uri:Uri)
//    }
//
//
//
////    override fun onClicking(position: Int) {
////        Toast.makeText(context, "app from list is clicked", Toast.LENGTH_SHORT).show()
////        //TODO App advertise list functionality add
////    }
//
//}