package com.example.soundnest_android.utils

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.example.soundnest_android.R

class FullMessageDialogFragment : DialogFragment() {

    interface OnFullMessageDialogListener {
        fun onFullMessageOk()
    }

    private var listener: OnFullMessageDialogListener? = null

    companion object {
        private const val ARG_MSG = "ARG_MSG"
        fun newInstance(message: String) = FullMessageDialogFragment().apply {
            arguments = Bundle().apply { putString(ARG_MSG, message) }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as? OnFullMessageDialogListener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = false
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val message = requireArguments().getString(ARG_MSG)
        val view = requireActivity().layoutInflater.inflate(R.layout.fragment_full_message, null)
        view.findViewById<TextView>(R.id.tvMessage).text = message

        val dialog = AlertDialog.Builder(requireContext())
            .setView(view)
            .setCancelable(false)
            .create()

        view.findViewById<Button>(R.id.btnOk).setOnClickListener {
            dialog.dismiss()
            listener?.onFullMessageOk()
        }

        return dialog
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }
}

