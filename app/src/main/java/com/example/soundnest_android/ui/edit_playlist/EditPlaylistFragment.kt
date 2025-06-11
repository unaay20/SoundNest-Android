package com.example.soundnest_android.ui.edit_playlist

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.soundnest_android.R

class EditPlaylistDialogFragment : DialogFragment() {

    private lateinit var etName: EditText
    private lateinit var etDescription: EditText
    private lateinit var playlistId: String

    var onPlaylistEdited: ((String, String, String?) -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            playlistId = it.getString(ARG_ID).orEmpty()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = LayoutInflater.from(requireContext())
        val view = inflater.inflate(R.layout.fragment_edit_playlist, null)

        etName = view.findViewById(R.id.editPlaylistName)
        etDescription = view.findViewById(R.id.editPlaylistDescription)

        arguments?.let {
            etName.setText(it.getString(ARG_NAME))
            etDescription.setText(it.getString(ARG_DESC))
        }

        return AlertDialog.Builder(requireContext())
            .setTitle(R.string.lbl_edit_playlist)
            .setView(view)
            .setPositiveButton(R.string.btn_save) { _, _ ->
                onPlaylistEdited?.invoke(
                    playlistId,
                    etName.text.toString(),
                    etDescription.text.toString().takeIf { it.isNotBlank() }
                )
            }
            .setNegativeButton(R.string.btn_cancel, null)
            .create()
    }

    companion object {
        private const val ARG_ID = "arg_id"
        private const val ARG_NAME = "arg_name"
        private const val ARG_DESC = "arg_desc"

        fun newInstance(
            id: String,
            name: String,
            description: String?
        ): EditPlaylistDialogFragment {
            return EditPlaylistDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_ID, id)
                    putString(ARG_NAME, name)
                    putString(ARG_DESC, description)
                }
            }
        }
    }
}
