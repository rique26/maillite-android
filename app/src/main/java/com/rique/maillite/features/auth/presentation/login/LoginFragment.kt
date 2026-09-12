package com.rique.maillite.features.auth.presentation.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.rique.maillite.databinding.FragmentLoginBinding
import dagger.hilt.android.AndroidEntryPoint

/**
 * STUB TEMPORÁRIO — existe só pra Splash ter um destino real de navegação.
 * Será substituído pela implementação completa quando chegarmos na tela de Login.
 */
@AndroidEntryPoint
class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
