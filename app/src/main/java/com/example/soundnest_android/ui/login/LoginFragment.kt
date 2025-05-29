package com.example.soundnest_android.ui.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.soundnest_android.R
import com.example.soundnest_android.auth.SharedPrefsTokenProvider
import com.example.soundnest_android.databinding.ActivityLoginBinding
import com.example.soundnest_android.ui.register.RegisterActivity
import com.example.soundnest_android.utils.Constants

class LoginFragment : Fragment() {
    private var _binding: ActivityLoginBinding? = null
    private val binding get() = _binding!!
    private val vm by lazy { ViewModelProvider(this).get(LoginViewModel::class.java) }

    private lateinit var tokenProvider: SharedPrefsTokenProvider

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

        tokenProvider = SharedPrefsTokenProvider(requireContext())

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

            vm.login(username, password)
        }

        binding.registerButton.setOnClickListener {
            startActivity(Intent(requireContext(), RegisterActivity::class.java))
        }

        vm.state.observe(viewLifecycleOwner) { state ->
            val enabled = state !is LoginState.Loading
            binding.loginButton.isEnabled = enabled
            binding.registerButton.isEnabled = enabled

            val invalidCredentialsText = getString(R.string.lbl_invalid_credentials)
            val userNotFoundText = getString(R.string.lbl_user_not_found)
            val failedToConnectText = getString(R.string.lbl_failed_to_connect)

            when (state) {
                is LoginState.Success -> {
                    tokenProvider.saveToken(state.data.token)
                    (activity as? LoginActivity)?.goToMain()
                }

                is LoginState.Error -> {
                    if (state.msg.contains(invalidCredentialsText)) {
                        Toast.makeText(requireContext(), state.msg, Toast.LENGTH_LONG).show()
                    } else if (state.msg.contains(userNotFoundText)) {
                        Toast.makeText(requireContext(), state.msg, Toast.LENGTH_LONG).show()
                    } else if (state.msg.contains(failedToConnectText)) {
                        Toast.makeText(
                            requireContext(),
                            R.string.msg_server_error,
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            R.string.msg_login_error,
                            Toast.LENGTH_LONG
                        ).show()
                        Log.d(Constants.LOGIN_ACTIVITY, "Error: ${state.msg}")
                    }
                }

                else -> {
                    // Idle o Loading: no-op
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
