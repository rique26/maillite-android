package com.rique.maillite.features.messages.presentation.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.rique.maillite.R
import com.rique.maillite.databinding.FragmentMessageDetailBinding
import dagger.hilt.android.AndroidEntryPoint

/**
 * STUB TEMPORÁRIO — existe só pra Inbox ter um destino real de navegação ao clicar num item.
 * Será substituído pela implementação completa quando chegarmos na tela de Detalhe da Mensagem.
 */
@AndroidEntryPoint
class MessageDetailFragment : Fragment() {

    private var _binding: FragmentMessageDetailBinding? = null
    private val binding get() = _binding!!

    private val args: MessageDetailFragmentArgs by navArgs()

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
        binding.textPlaceholderMessageDetail.text =
            getString(R.string.message_detail_placeholder, args.messageId)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
