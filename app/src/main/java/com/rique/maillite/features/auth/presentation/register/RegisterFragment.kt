package com.rique.maillite.features.auth.presentation.register

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.rique.maillite.databinding.FragmentRegisterBinding
import dagger.hilt.android.AndroidEntryPoint

/**
 * STUB TEMPORÁRIO — existe só pra Login ter um destino real de navegação.
 * Será substituído pela implementação completa quando chegarmos na tela de Registro.
 */
@AndroidEntryPoint
class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
