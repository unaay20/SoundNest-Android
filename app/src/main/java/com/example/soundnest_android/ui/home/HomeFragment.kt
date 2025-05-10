package com.example.soundnest_android.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.soundnest_android.R
import com.example.soundnest_android.auth.SharedPrefsTokenProvider
import com.example.soundnest_android.databinding.FragmentHomeBinding
import com.example.soundnest_android.ui.notifications.NotificationsActivity
import com.example.soundnest_android.ui.upload_song.UploadSongActivity

class HomeFragment : Fragment(R.layout.fragment_home) {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels()
    private lateinit var tokenProvider: SharedPrefsTokenProvider

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentHomeBinding.bind(view)
        super.onViewCreated(view, savedInstanceState)

        tokenProvider = SharedPrefsTokenProvider(requireContext())
        val name = tokenProvider.username ?: getString(R.string.lbl_mandatory)
        binding.textHome.text = getString(R.string.hello_user, name)

        binding.btnNotifications.setOnClickListener {
            viewModel.onNotificationsClicked()
        }
        viewModel.navigateToNotifications.observe(viewLifecycleOwner) { navigate ->
            if (navigate) {
                startActivity(Intent(requireContext(), NotificationsActivity::class.java))
                viewModel.onNavigated()
            }
        }

        binding.fabAddSong.setOnClickListener {
            viewModel.onAddSongClicked()
        }
        viewModel.navigateToUploadSong.observe(viewLifecycleOwner) { navigate ->
            if (navigate) {
                startActivity(Intent(requireContext(), UploadSongActivity::class.java))
                viewModel.onAddSongNavigated()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
