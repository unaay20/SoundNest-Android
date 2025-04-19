package com.example.soundnest_android.ui.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.soundnest_android.databinding.ActivityLoginBinding

class LoginFragment : Fragment() {

    private var _binding: ActivityLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ActivityLoginBinding.inflate(inflater, container, false)
        val root = binding.root

        binding.loginButton.setOnClickListener {
            (activity as? LoginActivity)?.goToMain()
        }

        //val textView = binding.navigationLogin
        val loginViewModel = ViewModelProvider(this).get(LoginViewModel::class.java)
        loginViewModel.text.observe(viewLifecycleOwner) {
            //textView.text = it
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
