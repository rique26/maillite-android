package com.rique.maillite.features.auth.presentation.register

import android.os.Bundle
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
import com.google.android.material.snackbar.Snackbar
import com.rique.maillite.R
import com.rique.maillite.databinding.FragmentRegisterBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private val viewModel: RegisterViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListeners()
        observeUiState()
    }

    private fun setupListeners() {
        binding.buttonRegister.setOnClickListener {
            val name = binding.inputNameRegister.text?.toString().orEmpty()
            val email = binding.inputEmailRegister.text?.toString().orEmpty()
            val password = binding.inputPasswordRegister.text?.toString().orEmpty()
            viewModel.register(name, email, password)
        }

        binding.textLoginLinkRegister.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state -> render(state) }
            }
        }
    }

    private fun render(state: RegisterUiState) {
        binding.progressBarRegister.visibility = if (state is RegisterUiState.Loading) View.VISIBLE else View.GONE
        binding.buttonRegister.isEnabled = state !is RegisterUiState.Loading

        when (state) {
            is RegisterUiState.Idle,
            is RegisterUiState.Loading -> Unit

            is RegisterUiState.Success -> {
                // Toast (em vez de Snackbar) porque a mensagem precisa sobreviver à navegação
                // de volta pro Login — o registro (RF01) não retorna token, então o fluxo
                // correto é o usuário fazer login em seguida (RF02), não entrar automaticamente.
                Toast.makeText(
                    requireContext(),
                    getString(R.string.register_success_message),
                    Toast.LENGTH_SHORT
                ).show()
                findNavController().popBackStack()
            }

            is RegisterUiState.Error -> {
                Snackbar.make(binding.root, state.message, Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
