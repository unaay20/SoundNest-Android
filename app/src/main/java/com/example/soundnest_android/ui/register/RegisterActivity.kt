package com.example.soundnest_android.ui.register

import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.soundnest_android.R
import com.example.soundnest_android.databinding.ActivityRegisterBinding
import com.example.soundnest_android.restful.models.user.AdditionalInformation

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private val vm by lazy { ViewModelProvider(this).get(RegisterViewModel::class.java) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSend.setOnClickListener {
            val user  = binding.etUsername.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val pass  = binding.etPassword.text.toString()

            if (user.isEmpty() || email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, R.string.msg_fields_required, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (pass.length < 6) {
                Toast.makeText(this, R.string.msg_password_too_weak, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            vm.sendCode(email)
        }

        vm.sendCodeState.observe(this) { state ->
            when (state) {
                SendCodeState.Loading -> {
                    binding.btnSend.isEnabled = false
                }
                is SendCodeState.Success -> {
                    binding.btnSend.isEnabled = true
                    showCodeDialog { code ->
                        val rawInfo = binding.etAdditionalInfo.text.toString().trim()

                        val additionalInfo = AdditionalInformation(rawInfo)

                        vm.register(
                            username              = binding.etUsername.text.toString().trim(),
                            email                 = binding.etEmail.text.toString().trim(),
                            password              = binding.etPassword.text.toString(),
                            code                  = code,
                            additionalInformation = additionalInfo
                        )
                    }
                }
                is SendCodeState.Error -> {
                    binding.btnSend.isEnabled = true
                    Toast.makeText(this, "Error sending code: ${state.msg}", Toast.LENGTH_LONG).show()
                }
                else -> Unit
            }
        }

        vm.state.observe(this) { state ->
            when (state) {
                RegisterState.Loading -> {
                    binding.btnSend.isEnabled = false
                    binding.btnCancel.isEnabled = false
                }
                is RegisterState.Success -> {
                    Toast.makeText(this, getString(R.string.lbl_register_success), Toast.LENGTH_LONG).show()
                    finish()
                }
                is RegisterState.Error -> {
                    Toast.makeText(this, "Error registering: ${state.msg}", Toast.LENGTH_LONG).show()
                    binding.btnSend.isEnabled = true
                    binding.btnCancel.isEnabled = true
                }
                else -> {
                    binding.btnSend.isEnabled = true
                    binding.btnCancel.isEnabled = true
                }
            }
        }

        binding.btnCancel.setOnClickListener { finish() }
    }

    private fun showCodeDialog(onConfirm: (String) -> Unit) {
        val input = EditText(this).apply {
            hint = getString(R.string.hint_code)
            inputType = InputType.TYPE_CLASS_TEXT
        }
        AlertDialog.Builder(this)
            .setTitle(R.string.action_verify)
            .setView(input)
            .setPositiveButton(R.string.action_verify) { dlg, _ ->
                val code = input.text.toString().trim()
                if (code.isBlank()) {
                    Toast.makeText(this, "Introduce un código válido", Toast.LENGTH_SHORT).show()
                } else {
                    onConfirm(code)
                    dlg.dismiss()
                }
            }
            .setNegativeButton(R.string.action_cancel) { dlg, _ -> dlg.dismiss() }
            .setCancelable(false)
            .show()
    }
}