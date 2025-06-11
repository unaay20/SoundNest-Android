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
import com.example.soundnest_android.restful.models.user.AdditionalInformation
import com.example.soundnest_android.restful.services.UserService
import com.example.soundnest_android.ui.change_password.ChangePasswordActivity

class EditProfileActivity : AppCompatActivity() {

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
                binding.etUsername.setText(profile.username)
            }

            val info = profile?.additionalInformation

            binding.etAdditionalInfo.setText(info.toString().trimEnd())
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
                Toast.makeText(this, R.string.msg_profile_saved, Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        binding.btnSaveProfile.setOnClickListener {
            val newUsername = binding.etUsername.text.toString().trim()
            val infoText = binding.etAdditionalInfo.text.toString()
            val additionalInfo = AdditionalInformation(infoText)
            viewModel.saveProfile(newUsername, additionalInfo)
        }

        binding.btnChangePassword.setOnClickListener {
            Intent(this, ChangePasswordActivity::class.java)
                .putExtra(ChangePasswordActivity.EXTRA_EMAIL, tokenProvider.email.orEmpty())
                .also(::startActivity)
        }
    }
}
