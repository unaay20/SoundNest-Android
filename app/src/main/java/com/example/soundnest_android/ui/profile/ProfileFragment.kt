package com.example.soundnest_android.ui.profile

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.soundnest_android.R
import com.example.soundnest_android.auth.SharedPrefsTokenProvider
import com.example.soundnest_android.databinding.FragmentProfileBinding
import com.example.soundnest_android.ui.login.LoginActivity

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return ProfileViewModel(
                    prefs         = SharedPrefsTokenProvider(requireContext()),
                    tokenProvider = { SharedPrefsTokenProvider(requireContext()).getToken() }
                ) as T
            }
        }
    }

    private val PICK_IMAGE = 1001

    private val userId: Int?
        get() = SharedPrefsTokenProvider(requireContext()).id

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentProfileBinding.bind(view)

        viewModel.profile.observe(viewLifecycleOwner) { p ->
            binding.tvUsername.text = p.username
            binding.tvEmail.   text = p.email
            binding.tvRole.    text = p.role
        }

        viewModel.photoBytes.observe(viewLifecycleOwner) { bytes ->
            Log.d("ProfileFrag", "photoBytes arrived: size=${bytes?.size}")
            if (bytes != null) {
                Glide.with(this)
                    .asBitmap()
                    .load(bytes)
                    .circleCrop()
                    .placeholder(R.drawable.ic_default_avatar)
                    .into(binding.ivProfilePhoto)
            } else {
                binding.ivProfilePhoto.setImageResource(R.drawable.ic_default_avatar)
            }
        }
        viewModel.error.observe(viewLifecycleOwner) { msg ->
            Log.e("ProfileFrag", "error: $msg")
            msg?.let { Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show() }
        }

        userId?.let { viewModel.loadProfileImage(it) }
            ?: Toast.makeText(requireContext(), "Usuario no autenticado", Toast.LENGTH_SHORT).show()

        binding.fabEditProfile.setOnClickListener {
            userId?.let {
                val intent = Intent(
                    Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                )
                startActivityForResult(intent, PICK_IMAGE)
            } ?: Toast.makeText(requireContext(), "No hay userId para subir foto", Toast.LENGTH_SHORT).show()
        }

        binding.btnEditProfile.setOnClickListener {
            startActivity(Intent(requireContext(), EditProfileActivity::class.java))
        }

        binding.btnLogout.setOnClickListener {
            SharedPrefsTokenProvider(requireContext()).clearSession()
            startActivity(
                Intent(requireContext(), LoginActivity::class.java)
                    .apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK }
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE
            && resultCode == Activity.RESULT_OK
            && data?.data != null
        ) {
            val uri = data.data!!
            requireContext().contentResolver.openInputStream(uri)?.use { stream ->
                val bytes = stream.readBytes()
                val ext = MimeTypeMap.getSingleton()
                    .getExtensionFromMimeType(
                        requireContext().contentResolver.getType(uri)
                    ) ?: "jpg"

                userId?.let { viewModel.uploadProfileImage(it, bytes, ext) }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
