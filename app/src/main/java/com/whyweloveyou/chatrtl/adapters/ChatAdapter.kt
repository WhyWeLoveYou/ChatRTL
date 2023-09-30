package com.whyweloveyou.chatrtl.adapters

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.whyweloveyou.chatrtl.R
import com.whyweloveyou.chatrtl.databinding.ItemContainerReceiveMsgBinding
import com.whyweloveyou.chatrtl.databinding.ItemContainerSentMsgBinding
import com.whyweloveyou.chatrtl.models.ChatMessage

class ChatAdapter : RecyclerView.Adapter<ViewHolder> {

    private lateinit var chatMessages: List<ChatMessage>
    private lateinit var receiverProfileImage: Bitmap
    private lateinit var senderId: String

    public val VIEW_TYPE_SENT: Int = 1
    public val VIEW_TYPE_RECEIVED: Int = 2

    constructor(chatMessages: List<ChatMessage>, receiverProfileImage: Bitmap, senderId: String) {
        this.chatMessages = chatMessages
        this.receiverProfileImage = receiverProfileImage
        this.senderId = senderId
    }

    override fun getItemCount(): Int {
        return chatMessages.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        if (viewType == VIEW_TYPE_SENT) {
            return SentMessageViewHolder(
                ItemContainerSentMsgBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        } else {
            return ReceiveMessageViewHolder(
                ItemContainerReceiveMsgBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (getItemViewType(position) == VIEW_TYPE_SENT) {
            (holder as SentMessageViewHolder).setData(chatMessages.get(position))
        } else {
            (holder as ReceiveMessageViewHolder).setData(
                chatMessages.get(position),
                receiverProfileImage
            )
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (chatMessages.get(position).senderId.equals(senderId)) {
            return VIEW_TYPE_SENT
        } else {
            return VIEW_TYPE_RECEIVED
        }
    }

    inner class SentMessageViewHolder(private var itemContainerSentMsgBinding: ItemContainerSentMsgBinding) :
        ViewHolder(itemContainerSentMsgBinding.root) {

        fun setData(chatMessage: ChatMessage) {
            itemContainerSentMsgBinding.textMessage.text = chatMessage.message
            itemContainerSentMsgBinding.textDate.text = chatMessage.dateTime

            itemContainerSentMsgBinding.textMessage.setTextColor(ContextCompat.getColor(itemView.context, R.color.black))
        }
    }

    inner class ReceiveMessageViewHolder(private var itemContainerReceiveMsgBinding: ItemContainerReceiveMsgBinding) :
        ViewHolder(itemContainerReceiveMsgBinding.root) {

        fun setData(chatMessage: ChatMessage, receiverProfileImage: Bitmap) {
            itemContainerReceiveMsgBinding.textMessage.text = chatMessage.message
            itemContainerReceiveMsgBinding.textDate.text = chatMessage.dateTime
            itemContainerReceiveMsgBinding.imageProfilee.setImageBitmap(receiverProfileImage)

            itemContainerReceiveMsgBinding.textMessage.setTextColor(ContextCompat.getColor(itemView.context, R.color.black))
        }
    }
}