package com.example.soundnest_android.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.example.soundnest_android.R
import com.example.soundnest_android.databinding.FragmentProfileBinding
import com.example.soundnest_android.ui.login.LoginActivity

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentProfileBinding.bind(view)
        super.onViewCreated(view, savedInstanceState)

        viewModel.profile.observe(viewLifecycleOwner) { profile ->
            binding.tvUsername.text = profile.username
            binding.tvEmail.text    = profile.email
            binding.tvRole.text     = profile.role

            if (profile.photoUrl.isNullOrBlank()) {
                binding.ivProfilePhoto.setImageResource(R.drawable.ic_default_avatar)
            } else {
                Glide.with(this)
                    .load(profile.photoUrl)
                    .circleCrop()
                    .placeholder(R.drawable.ic_default_avatar)
                    .into(binding.ivProfilePhoto)
            }
        }


        binding.btnEditProfile.setOnClickListener {
            viewModel.onEditClicked()
        }
        viewModel.editEvent.observe(viewLifecycleOwner) {
            val intent = Intent(requireContext(), EditProfileActivity::class.java)
            startActivity(intent)
        }

        binding.btnLogout.setOnClickListener {
            viewModel.onLogoutClicked()
        }
        viewModel.logoutEvent.observe(viewLifecycleOwner) {
            val intent = Intent(requireActivity(), LoginActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
