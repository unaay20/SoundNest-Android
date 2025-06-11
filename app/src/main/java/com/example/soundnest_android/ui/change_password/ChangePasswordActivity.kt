package com.example.soundnest_android.ui.change_password

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.soundnest_android.R
import com.example.soundnest_android.auth.SharedPrefsTokenProvider
import com.example.soundnest_android.databinding.ActivityChangePasswordBinding
import com.example.soundnest_android.utils.Constants

class ChangePasswordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChangePasswordBinding
    private val vm by lazy { ViewModelProvider(this).get(ChangePasswordViewModel::class.java) }

    private val email: String by lazy {
        intent.getStringExtra(EXTRA_EMAIL)
            ?: throw IllegalArgumentException(R.string.exception_extra_email.toString())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangePasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        vm.sendCode(email)

        vm.sendCodeState.observe(this) { state ->
            when (state) {
                SendCodeState.Loading -> {
                    binding.btnChangePassword.isEnabled = false
                }

                is SendCodeState.Success -> {
                    binding.btnChangePassword.isEnabled = true
                    val message = getString(R.string.msg_code_sent, email)
                    Toast.makeText(this, (message), Toast.LENGTH_LONG).show()
                }

                is SendCodeState.Error -> {
                    binding.btnChangePassword.isEnabled = true
                    Toast.makeText(this, R.string.msg_code_error, Toast.LENGTH_LONG).show()
                    Log.d(Constants.CHANGE_PASSWORD_ACTIVITY, state.msg)
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
                    Toast.makeText(this, R.string.msg_enter_code, Toast.LENGTH_SHORT).show()

                newPass.length < 6 ->
                    Toast.makeText(this, R.string.msg_password_too_weak, Toast.LENGTH_SHORT).show()

                newPass != repeat ->
                    Toast.makeText(this, R.string.msg_password_match, Toast.LENGTH_SHORT).show()

                else -> {
                    val email = SharedPrefsTokenProvider(this).email
                    if (email.isNullOrBlank()) {
                        Toast.makeText(this, "No email in token", Toast.LENGTH_SHORT).show()
                    } else {
                        vm.changePassword(email, code, newPass)
                    }
                }
            }
        }

        vm.changeState.observe(this) { state ->
            when (state) {
                ChangePasswordState.Loading -> {
                    binding.btnChangePassword.isEnabled = false
                    binding.btnCancel.isEnabled = false
                }

                is ChangePasswordState.Success -> {
                    Toast.makeText(this, R.string.msg_exit_change_password, Toast.LENGTH_LONG)
                        .show()
                    finish()
                }

                is ChangePasswordState.Error -> {
                    Toast.makeText(this, R.string.msg_error_change_password, Toast.LENGTH_LONG)
                        .show()
                    Log.d(Constants.CHANGE_PASSWORD_ACTIVITY, state.msg)
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
