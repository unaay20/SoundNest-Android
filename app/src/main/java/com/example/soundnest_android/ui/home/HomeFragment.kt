package com.example.soundnest_android.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.soundnest_android.R
import com.example.soundnest_android.databinding.FragmentHomeBinding
import com.example.soundnest_android.ui.notifications.NotificationsActivity

class HomeFragment : Fragment(R.layout.fragment_home) {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentHomeBinding.bind(view)
        super.onViewCreated(view, savedInstanceState)

        binding.btnNotifications.setOnClickListener {
            viewModel.onNotificationsClicked()
        }

        viewModel.navigateToNotifications.observe(viewLifecycleOwner) { navigate ->
            if (navigate) {
                startActivity(Intent(requireContext(), NotificationsActivity::class.java))
                viewModel.onNavigated()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
