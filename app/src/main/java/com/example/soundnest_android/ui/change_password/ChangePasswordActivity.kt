package com.example.soundnest_android.ui.change_password

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.soundnest_android.databinding.ActivityChangePasswordBinding

class ChangePasswordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChangePasswordBinding
    private val vm by lazy { ViewModelProvider(this).get(ChangePasswordViewModel::class.java) }

    private val email: String by lazy {
        intent.getStringExtra(EXTRA_EMAIL)
            ?: throw IllegalArgumentException("Se requiere EXTRA_EMAIL en el Intent")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangePasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1) Enviar código al iniciar
        vm.sendCode(email)

        vm.sendCodeState.observe(this) { state ->
            when (state) {
                SendCodeState.Loading -> {
                    binding.btnChangePassword.isEnabled = false
                }
                is SendCodeState.Success -> {
                    binding.btnChangePassword.isEnabled = true
                    Toast.makeText(this, "Código enviado a $email", Toast.LENGTH_LONG).show()
                }
                is SendCodeState.Error -> {
                    binding.btnChangePassword.isEnabled = true
                    Toast.makeText(this, "Error enviando código: ${state.msg}", Toast.LENGTH_LONG).show()
                }
                else -> Unit
            }
        }

        binding.btnChangePassword.setOnClickListener {
            val code = binding.etVerificationCode.text.toString().trim()
            val newPass = binding.etNewPassword.text.toString()
            val repeat = binding.etRepeatNewPassword.text.toString()

            when {
                code.isEmpty() ->
                    Toast.makeText(this, "Introduce el código", Toast.LENGTH_SHORT).show()
                newPass.length < 6 ->
                    Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
                newPass != repeat ->
                    Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                //else -> vm.changePassword(email, code, newPass)
            }
        }

        vm.changeState.observe(this) { state ->
            when (state) {
                ChangePasswordState.Loading -> {
                    binding.btnChangePassword.isEnabled = false
                    binding.btnCancel.isEnabled = false
                }
                is ChangePasswordState.Success -> {
                    Toast.makeText(this, "Contraseña cambiada con éxito", Toast.LENGTH_LONG).show()
                    finish()
                }
                is ChangePasswordState.Error -> {
                    Toast.makeText(this, "Error cambiando contraseña: ${state.msg}", Toast.LENGTH_LONG).show()
                    binding.btnChangePassword.isEnabled = true
                    binding.btnCancel.isEnabled = true
                }
                else -> {
                    binding.btnChangePassword.isEnabled = true
                    binding.btnCancel.isEnabled = true
                }
            }
        }

        binding.btnCancel.setOnClickListener { finish() }
    }

    companion object {
        const val EXTRA_EMAIL = "extra_email"
    }
}
