package com.example.soundnest_android.utils

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.example.soundnest_android.R

class FullMessageDialogFragment : DialogFragment() {
    companion object {
        private const val ARG_MSG = "ARG_MSG"
        fun newInstance(message: String) = FullMessageDialogFragment().apply {
            arguments = Bundle().apply { putString(ARG_MSG, message) }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val message = requireArguments().getString(ARG_MSG)
        val view = requireActivity().layoutInflater.inflate(R.layout.fragment_full_message, null)
        view.findViewById<TextView>(R.id.tvMessage).text = message

        val dialog = AlertDialog.Builder(requireContext())
            .setView(view)
            .create()

        view.findViewById<Button>(R.id.btnOk).setOnClickListener {
            dialog.dismiss()
        }
        return dialog
    }
}
