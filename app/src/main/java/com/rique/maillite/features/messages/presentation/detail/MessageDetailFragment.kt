package com.rique.maillite.features.messages.presentation.detail

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.snackbar.Snackbar
import com.rique.maillite.R
import com.rique.maillite.core.util.AvatarUtil
import com.rique.maillite.databinding.FragmentMessageDetailBinding
import com.rique.maillite.features.messages.domain.model.Message
import com.rique.maillite.features.messages.presentation.MessageDetailResult
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class MessageDetailFragment : Fragment() {

    private var _binding: FragmentMessageDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MessageDetailViewModel by viewModels()
    private val args: MessageDetailFragmentArgs by navArgs()

    private val dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMessageDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListeners()
        observeUiState()
        observeDeleteError()
    }

    private fun observeDeleteError() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.deleteError.collect { message ->
                    Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupListeners() {
        binding.buttonDeleteMessageDetail.setOnClickListener {
            viewModel.delete()
        }
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state -> render(state) }
            }
        }
    }

    private fun render(state: MessageDetailUiState) {
        binding.progressBarMessageDetail.visibility = toVisibility(state is MessageDetailUiState.Loading)
        binding.layoutContentMessageDetail.visibility = toVisibility(state is MessageDetailUiState.Success)
        binding.textErrorMessageDetail.visibility = toVisibility(state is MessageDetailUiState.Error)

        when (state) {
            is MessageDetailUiState.Loading,
            is MessageDetailUiState.Deleted -> Unit

            is MessageDetailUiState.Success -> {
                bindMessage(state.message)
                notifyInbox(state.message.id, MessageDetailResult.ACTION_READ)
            }

            is MessageDetailUiState.Error -> {
                binding.textErrorMessageDetail.text = state.message
            }
        }

        if (state is MessageDetailUiState.Deleted) {
            notifyInbox(args.messageId, MessageDetailResult.ACTION_DELETED)

            // Toast (em vez de Snackbar) porque a mensagem precisa sobreviver à navegação
            // de volta pra Inbox — mesmo padrão usado no Registro e na Compor Mensagem.
            Toast.makeText(
                requireContext(),
                getString(R.string.message_detail_deleted_message),
                Toast.LENGTH_SHORT
            ).show()
            findNavController().popBackStack()
        }
    }

    private fun notifyInbox(messageId: Long, action: String) {
        parentFragmentManager.setFragmentResult(
            MessageDetailResult.REQUEST_KEY,
            bundleOf(
                MessageDetailResult.KEY_MESSAGE_ID to messageId,
                MessageDetailResult.KEY_ACTION to action
            )
        )
    }

    private fun bindMessage(message: Message) {
        binding.textSenderNameMessageDetail.text = message.sender.name
        binding.textSenderEmailMessageDetail.text = message.sender.email
        binding.textSentAtMessageDetail.text = message.sentAt.format(dateTimeFormatter)
        binding.textSubjectMessageDetail.text = message.subject
        binding.textBodyMessageDetail.text = message.body

        binding.avatarMessageDetail.text = AvatarUtil.initialsOf(message.sender.name)
        binding.avatarMessageDetail.backgroundTintList = ColorStateList.valueOf(
            ContextCompat.getColor(requireContext(), AvatarUtil.colorResFor(message.sender.name))
        )
    }

    private fun toVisibility(condition: Boolean): Int = if (condition) View.VISIBLE else View.GONE

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}