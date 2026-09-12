package com.rique.maillite.features.messages.presentation.compose.adapter

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rique.maillite.core.util.AvatarUtil
import com.rique.maillite.databinding.ItemRecipientSuggestionBinding
import com.rique.maillite.features.users.domain.model.User

class RecipientSuggestionsAdapter(
    private val onSuggestionClick: (User) -> Unit
) : ListAdapter<User, RecipientSuggestionsAdapter.SuggestionViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SuggestionViewHolder {
        val binding = ItemRecipientSuggestionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return SuggestionViewHolder(binding, onSuggestionClick)
    }

    override fun onBindViewHolder(holder: SuggestionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class SuggestionViewHolder(
        private val binding: ItemRecipientSuggestionBinding,
        private val onSuggestionClick: (User) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(user: User) {
            binding.textNameItemRecipientSuggestion.text = user.name
            binding.textEmailItemRecipientSuggestion.text = user.email

            binding.avatarItemRecipientSuggestion.text = AvatarUtil.initialsOf(user.name)
            binding.avatarItemRecipientSuggestion.backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(binding.root.context, AvatarUtil.colorResFor(user.name))
            )

            binding.root.setOnClickListener { onSuggestionClick(user) }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<User>() {
            override fun areItemsTheSame(oldItem: User, newItem: User): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: User, newItem: User): Boolean =
                oldItem == newItem
        }
    }
}
