package com.example.soundnest_android.ui.profile

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.soundnest_android.auth.SharedPrefsTokenProvider
import com.example.soundnest_android.databinding.ActivityEditProfileBinding
import com.example.soundnest_android.ui.change_password.ChangePasswordActivity

class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding
    private val viewModel: EditProfileViewModel by viewModels()

    private lateinit var tokenProvider: SharedPrefsTokenProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tokenProvider = SharedPrefsTokenProvider(this)
        val currentUsername = tokenProvider.username.orEmpty()
        val currentEmail    = tokenProvider.email.orEmpty()

        binding.etUsername.setText(currentUsername)
        binding.etAdditionalInfo   .setText(currentEmail)

        viewModel.onLoadInitial( currentUsername, currentEmail )

        viewModel.profile.observe(this) { profile ->
            profile.photoUrl?.let {
                Glide.with(this)
                    .load(it)
                    .circleCrop()
                    .into(binding.ivEditProfilePhoto)
            }
        }

        binding.btnSaveProfile.setOnClickListener {
            val newUsername = binding.etUsername.text.toString().trim()
            val newEmail    = binding.etAdditionalInfo   .text.toString().trim()
            viewModel.onSaveClicked(newUsername, newEmail)
        }

        viewModel.saveResult.observe(this) { success ->
            if (success) {
                Toast.makeText(this, "Perfil actualizado", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Error al guardar", Toast.LENGTH_SHORT).show()
            }
        }

        // 4) Botón Change Password
        binding.btnChangePassword.setOnClickListener {
            // Lanza ChangePasswordActivity pasando el email actual
            val intent = Intent(this, ChangePasswordActivity::class.java)
                .putExtra(ChangePasswordActivity.EXTRA_EMAIL, currentEmail)
            startActivity(intent)
        }
    }
}
