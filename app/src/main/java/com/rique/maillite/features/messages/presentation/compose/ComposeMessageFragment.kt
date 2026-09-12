package com.rique.maillite.features.messages.presentation.compose

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.rique.maillite.databinding.FragmentComposeMessageBinding
import dagger.hilt.android.AndroidEntryPoint

/**
 * STUB TEMPORÁRIO — existe só pra Inbox ter um destino real de navegação a partir do FAB.
 * Será substituído pela implementação completa quando chegarmos na tela de Compor Mensagem.
 */
@AndroidEntryPoint
class ComposeMessageFragment : Fragment() {

    private var _binding: FragmentComposeMessageBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentComposeMessageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
