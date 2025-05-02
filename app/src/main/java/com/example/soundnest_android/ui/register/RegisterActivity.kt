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

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private val vm by lazy { ViewModelProvider(this).get(RegisterViewModel::class.java) }

    private val USE_FAKE_REGISTER = true
    private val FAKE_CODE = "1234"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSend.setOnClickListener {
            val user  = binding.etUsername.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val pass  = binding.etPassword.text.toString()

            // Validación de vacíos...
            var valid = true
            if (user.isEmpty()) {
                binding.tilUsername.error = getString(R.string.lbl_mandatory)
                valid = false
            } else binding.tilUsername.error = null

            if (email.isEmpty()) {
                binding.tilEmail.error = getString(R.string.lbl_mandatory)
                valid = false
            } else binding.tilEmail.error = null

            if (pass.isEmpty()) {
                binding.tilPassword.error = getString(R.string.lbl_mandatory)
                valid = false
            } else binding.tilPassword.error = null

            if (!valid) return@setOnClickListener

            if (USE_FAKE_REGISTER) {
                showCodeVerificationDialog(email) { codeEntered ->
                    if (codeEntered == FAKE_CODE) {
                        Toast.makeText(this, getString(R.string.lbl_register_success), Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this, R.string.lbl_invalid_code, Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                vm.register(user, email, pass)
            }
        }

        binding.btnCancel.setOnClickListener {
            finish()
        }

        vm.state.observe(this) { state ->
            val enabled = state !is RegisterState.Loading
            binding.btnSend.isEnabled   = enabled
            binding.btnCancel.isEnabled = enabled

            when (state) {
                is RegisterState.Success -> {
                    showCodeVerificationDialog(binding.etEmail.text.toString()) { entered ->
                        // TODO: llama a tu endpoint de verificación real aquí
                        Toast.makeText(this, getString(R.string.lbl_register_success), Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
                is RegisterState.Error -> {
                    Toast.makeText(this, getString(R.string.lbl_register_error, state.msg), Toast.LENGTH_LONG).show()
                }
                else -> { /* Idle o Loading */ }
            }
        }
    }


    private fun showCodeVerificationDialog(
        email: String,
        onConfirm: (code: String) -> Unit
    ) {
        val input = EditText(this).apply {
            hint = getString(R.string.hint_code)
            inputType = InputType.TYPE_CLASS_NUMBER
        }

        AlertDialog.Builder(this)
            .setTitle(R.string.action_verify)
            .setMessage(getString(R.string.msg_enter_code, email))
            .setView(input)
            .setPositiveButton(R.string.action_verify) { dialog, _ ->
                onConfirm(input.text.toString().trim())
                dialog.dismiss()
            }
            .setNegativeButton(R.string.action_cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(false)
            .show()
    }
}
