package com.example.soundnest_android.ui.change_password

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.soundnest_android.R
import com.example.soundnest_android.auth.SharedPrefsTokenProvider
import com.example.soundnest_android.databinding.ActivityChangePasswordBinding
import com.example.soundnest_android.ui.login.LoginActivity
import com.example.soundnest_android.utils.Constants
import com.example.soundnest_android.utils.FullMessageDialogFragment

class ChangePasswordActivity : AppCompatActivity(),
    FullMessageDialogFragment.OnFullMessageDialogListener {
    private lateinit var binding: ActivityChangePasswordBinding
    private val vm by lazy { ViewModelProvider(this).get(ChangePasswordViewModel::class.java) }

    private val email: String by lazy {
        intent.getStringExtra(EXTRA_EMAIL)
            ?: throw IllegalArgumentException(getString(R.string.exception_extra_email))
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
                    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                }

                is SendCodeState.Error -> {
                    binding.btnChangePassword.isEnabled = true
                    FullMessageDialogFragment.newInstance(state.msg)
                        .show(supportFragmentManager, "SEND_CODE_ERROR")
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
                    vm.changePassword(email, code, newPass)
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
                    FullMessageDialogFragment.newInstance(
                        getString(R.string.msg_password_changed),
                    ).show(supportFragmentManager, "RELOGIN_DIALOG")
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

    override fun onFullMessageOk() {
        SharedPrefsTokenProvider(this).clearSession()
        Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }.also(::startActivity)
    }
}
