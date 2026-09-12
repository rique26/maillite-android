package com.rique.maillite.features.messages.presentation.compose

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.rique.maillite.R
import com.rique.maillite.databinding.FragmentComposeMessageBinding
import com.rique.maillite.features.messages.presentation.compose.ComposeMessageUiState.SendStatus
import com.rique.maillite.features.messages.presentation.compose.adapter.RecipientSuggestionsAdapter
import com.rique.maillite.features.users.domain.model.User
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ComposeMessageFragment : Fragment() {

    private var _binding: FragmentComposeMessageBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ComposeMessageViewModel by viewModels()

    private val suggestionsAdapter = RecipientSuggestionsAdapter(onSuggestionClick = ::onSuggestionSelected)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentComposeMessageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupSuggestionsList()
        setupListeners()
        observeUiState()
    }

    private fun setupSuggestionsList() {
        binding.recyclerSuggestionsComposeMessage.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = suggestionsAdapter
        }
    }

    private fun setupListeners() {
        // afterTextChanged (não TextWatcher completo) é suficiente aqui: só precisamos do
        // texto final pra disparar a busca com debounce lá no ViewModel.
        binding.inputRecipientComposeMessage.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            override fun afterTextChanged(s: Editable?) {
                viewModel.onRecipientQueryChanged(s?.toString().orEmpty())
            }
        })

        binding.buttonSendComposeMessage.setOnClickListener {
            val subject = binding.inputSubjectComposeMessage.text?.toString().orEmpty()
            val body = binding.inputBodyComposeMessage.text?.toString().orEmpty()
            viewModel.send(subject, body)
        }
    }

    private fun onSuggestionSelected(user: User) {
        viewModel.selectRecipient(user)
        // Preenche o campo com o nome escolhido. Isso dispara o TextWatcher de novo,
        // mas o ViewModel reconhece que o texto já bate com a seleção e não refaz a busca.
        binding.inputRecipientComposeMessage.apply {
            setText(user.name)
            setSelection(user.name.length)
        }
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state -> render(state) }
            }
        }
    }

    private fun render(state: ComposeMessageUiState) {
        suggestionsAdapter.submitList(state.suggestions)
        binding.cardSuggestionsComposeMessage.visibility =
            if (state.suggestions.isNotEmpty()) View.VISIBLE else View.GONE

        val isSending = state.sendStatus is SendStatus.Sending
        binding.progressBarComposeMessage.visibility = if (isSending) View.VISIBLE else View.GONE
        binding.buttonSendComposeMessage.isEnabled = !isSending

        when (val status = state.sendStatus) {
            is SendStatus.Idle,
            is SendStatus.Sending -> Unit

            is SendStatus.Sent -> {
                // Toast (em vez de Snackbar) porque a mensagem precisa sobreviver à navegação
                // de volta pra Inbox — mesmo padrão usado no Registro.
                Toast.makeText(
                    requireContext(),
                    getString(R.string.compose_message_success),
                    Toast.LENGTH_SHORT
                ).show()
                findNavController().popBackStack()
            }

            is SendStatus.Error -> {
                Snackbar.make(binding.root, status.message, Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.recyclerSuggestionsComposeMessage.adapter = null
        _binding = null
    }
}
