package com.example.soundnest_android.ui.notifications

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.soundnest_android.R
import com.example.soundnest_android.databinding.ActivityNotificationsBinding

class NotificationsFragment : Fragment(R.layout.activity_notifications) {

    private var _binding: ActivityNotificationsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: NotificationsViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = ActivityNotificationsBinding.bind(view)
        super.onViewCreated(view, savedInstanceState)

        // Adaptador simple para ListView
        val adapter = ArrayAdapter<String>(
            requireContext(),
            android.R.layout.simple_list_item_1,
            mutableListOf()
        )
        binding.lvNotifications.adapter = adapter

        // Observa las notificaciones y actualiza la lista
        viewModel.notifications.observe(viewLifecycleOwner) { list ->
            adapter.clear()
            adapter.addAll(list)
            adapter.notifyDataSetChanged()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
