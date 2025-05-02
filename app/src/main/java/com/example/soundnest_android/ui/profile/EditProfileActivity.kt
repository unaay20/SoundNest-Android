package com.example.soundnest_android.ui.profile

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.soundnest_android.databinding.ActivityEditProfileBinding

class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding
    private val viewModel: EditProfileViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel.profile.observe(this) { profile ->
            binding.etUsername.setText(profile.username)
            binding.etEmail.setText(profile.email)

            profile.photoUrl?.let {
                Glide.with(this)
                    .load(it)
                    .circleCrop()
                    .into(binding.ivEditProfilePhoto)
            }
        }

        binding.btnSaveProfile.setOnClickListener {
            val newUsername = binding.etUsername.text.toString().trim()
            val newEmail = binding.etEmail.text.toString().trim()
            viewModel.onSaveClicked(newUsername, newEmail)
        }

        viewModel.saveResult.observe(this) { success ->
            if (success) {
                Toast.makeText(this, "Perfil actualizado", Toast.LENGTH_SHORT).show()
                finish()  // cierra la Activity y regresa
            } else {
                Toast.makeText(this, "Error al guardar", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
