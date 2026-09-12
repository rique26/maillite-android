package com.rique.maillite.features.messages.presentation.inbox.adapter

import android.graphics.Typeface
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rique.maillite.databinding.ItemMessageBinding
import com.rique.maillite.features.messages.domain.model.Message
import java.time.format.DateTimeFormatter

class MessagesAdapter(
    private val onMessageClick: (Message) -> Unit
) : ListAdapter<Message, MessagesAdapter.MessageViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val binding = ItemMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MessageViewHolder(binding, onMessageClick)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class MessageViewHolder(
        private val binding: ItemMessageBinding,
        private val onMessageClick: (Message) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(message: Message) {
            binding.textSenderNameItemMessage.text = message.sender.name
            binding.textSubjectItemMessage.text = message.subject
            binding.textSentAtItemMessage.text = message.sentAt.format(TIME_FORMATTER)

            val typeface = if (!message.read) Typeface.BOLD else Typeface.NORMAL
            binding.textSenderNameItemMessage.setTypeface(null, typeface)
            binding.textSubjectItemMessage.setTypeface(null, typeface)

            binding.dotUnreadItemMessage.visibility = if (!message.read) View.VISIBLE else View.GONE

            binding.root.setOnClickListener { onMessageClick(message) }
        }
    }

    companion object {
        @RequiresApi(Build.VERSION_CODES.O)
        private val TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM HH:mm")

        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Message>() {
            override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean =
                oldItem == newItem
        }
    }
}
