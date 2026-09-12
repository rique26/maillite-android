package com.rique.maillite.features.messages.presentation.inbox

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.rique.maillite.databinding.FragmentInboxBinding
import dagger.hilt.android.AndroidEntryPoint

/**
 * STUB TEMPORÁRIO — existe só pra Splash ter um destino real de navegação.
 * Será substituído pela implementação completa quando chegarmos na tela de Inbox.
 */
@AndroidEntryPoint
class InboxFragment : Fragment() {

    private var _binding: FragmentInboxBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInboxBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
