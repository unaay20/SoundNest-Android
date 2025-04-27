package com.example.soundnest_android.ui.login

import LoginViewModel
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.soundnest_android.R
import com.example.soundnest_android.databinding.ActivityLoginBinding
import com.example.soundnest_android.ui.register.RegisterActivity

class LoginFragment : Fragment() {
    private var _binding: ActivityLoginBinding? = null
    private val binding get() = _binding!!
    private val vm by lazy { ViewModelProvider(this).get(LoginViewModel::class.java) }

    private val USE_FAKE_LOGIN = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ActivityLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.loginButton.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            val password = binding.etPassword.text.toString()


            var valid = true
            if (username.isEmpty()) {
                binding.tilUsername.error = getString(R.string.lbl_mandatory)
                valid = false
            } else {
                binding.tilUsername.error = null
            }
            if (password.isEmpty()) {
                binding.tilPassword.error = getString(R.string.lbl_mandatory)
                valid = false
            } else {
                binding.tilPassword.error = null
            }
            if (!valid) return@setOnClickListener

            //TODO delete this
            (activity as? LoginActivity)?.goToMain()
            //vm.login(username, password)
        }

        binding.registerButton.setOnClickListener {
            startActivity(Intent(requireContext(), RegisterActivity::class.java))
        }

        vm.state.observe(viewLifecycleOwner) { state ->
            // mientras carga, deshabilita botones
            val enabled = state !is LoginState.Loading
            binding.loginButton.isEnabled = enabled
            binding.registerButton.isEnabled = enabled

            when (state) {
                is LoginState.Success -> (activity as? LoginActivity)?.goToMain()
                is LoginState.Error   -> Toast.makeText(requireContext(), state.msg, Toast.LENGTH_LONG).show()
                else                  -> { /* Idle o Loading */ }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
