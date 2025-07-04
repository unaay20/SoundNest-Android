package com.example.soundnest_android.ui.edit_profile

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.soundnest_android.R
import com.example.soundnest_android.auth.SharedPrefsTokenProvider
import com.example.soundnest_android.databinding.ActivityEditProfileBinding
import com.example.soundnest_android.restful.constants.RestfulRoutes
import com.example.soundnest_android.restful.services.UserService
import com.example.soundnest_android.ui.change_password.ChangePasswordActivity
import com.example.soundnest_android.ui.login.LoginActivity
import com.example.soundnest_android.utils.FullMessageDialogFragment

class EditProfileActivity : AppCompatActivity(),
    FullMessageDialogFragment.OnFullMessageDialogListener {

    private var originalUsername: String = ""
    private var originalAdditionalInfo: String = ""
    private lateinit var binding: ActivityEditProfileBinding
    private lateinit var tokenProvider: SharedPrefsTokenProvider
    private val factory by lazy {
        EditProfileViewModelFactory(
            application,
            UserService(RestfulRoutes.getBaseUrl(), tokenProvider),
            SharedPrefsTokenProvider(this)
        )
    }
    private val viewModel: EditProfileViewModel by viewModels { factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        tokenProvider = SharedPrefsTokenProvider(this)

        viewModel.profile.observe(this) { profile ->
            if (profile != null) {
                val username = profile.username
                val info = profile.additionalInformation.orEmpty()

                originalUsername = username
                originalAdditionalInfo = info

                binding.etUsername.setText(username)
                binding.etAdditionalInfo.setText(info.trimEnd())
            }
        }

        viewModel.photoBytes.observe(this) { bytes ->
            if (bytes != null) {
                Glide.with(this)
                    .asBitmap()
                    .load(bytes)
                    .circleCrop()
                    .placeholder(R.drawable.ic_default_avatar)
                    .into(binding.ivEditProfilePhoto)
            } else {
                binding.ivEditProfilePhoto.setImageResource(R.drawable.ic_default_avatar)
            }
        }

        viewModel.errorEvent.observe(this) { msg ->
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }

        viewModel.saveResult.observe(this) { success ->
            if (success) {
                FullMessageDialogFragment.newInstance(
                    getString(R.string.msg_profile_edited),
                ).show(supportFragmentManager, "RELOGIN_DIALOG")
            }
        }

        binding.btnSaveProfile.setOnClickListener {
            val newUsername = binding.etUsername.text.toString().trim()
            val newInfo = binding.etAdditionalInfo.text.toString()

            if (newUsername == originalUsername && newInfo == originalAdditionalInfo) {
                Toast.makeText(this, R.string.msg_no_changes_made, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.saveProfile(newUsername, newInfo)
        }

        binding.btnChangePassword.setOnClickListener {
            Intent(this, ChangePasswordActivity::class.java)
                .putExtra(ChangePasswordActivity.EXTRA_EMAIL, tokenProvider.email.orEmpty())
                .also(::startActivity)
        }
    }

    override fun onFullMessageOk() {
        SharedPrefsTokenProvider(this).clearSession()

        Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }.also { startActivity(it) }
    }

}
