package com.example.soundnest_android.recover_password

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import com.example.soundnest_android.databinding.FragmentRecoverPasswordBinding
import com.example.soundnest_android.ui.change_password.ChangePasswordActivity
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class RecoverPasswordDialogFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentRecoverPasswordBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecoverPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        binding.btnContinue.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()

            if (email.isEmpty()) {
                binding.tilEmail.error = "Este campo es obligatorio"
            } else {
                binding.tilEmail.error = null
                val intent = Intent(requireContext(), ChangePasswordActivity::class.java)
                intent.putExtra("extra_email", email)
                startActivity(intent)
                dismiss()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}