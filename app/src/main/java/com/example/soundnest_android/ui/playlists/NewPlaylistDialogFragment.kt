package com.example.soundnest_android.ui.playlists

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.EditText
import android.widget.ImageView
import androidx.fragment.app.DialogFragment
import com.example.soundnest_android.R

class NewPlaylistDialogFragment : DialogFragment() {

    var onPlaylistCreated: ((String, String, String?) -> Unit)? = null
    private lateinit var playlistImageView: ImageView
    private var selectedImageUri: Uri? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity())
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.fragment_new_playlist, null)

        val playlistNameEditText: EditText = view.findViewById(R.id.editTextPlaylistName)
        val playlistDescriptionEditText: EditText = view.findViewById(R.id.editTextPlaylistDescription)
        playlistImageView = view.findViewById(R.id.editTextPlaylistImage)

        playlistImageView.setOnClickListener {
            openImagePicker()
        }

        builder.setView(view)
            .setTitle("Crear Nueva Playlist")
            .setPositiveButton("Crear") { dialog, id ->
                val name = playlistNameEditText.text.toString()
                val description = playlistDescriptionEditText.text.toString()

                val imageUri = selectedImageUri?.toString() ?: "img_party_background"

                onPlaylistCreated?.invoke(name, description, imageUri)
            }
            .setNegativeButton("Cancelar") { dialog, id ->
                dialog.dismiss()
            }

        return builder.create()
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == -1 && requestCode == PICK_IMAGE_REQUEST) {
            selectedImageUri = data?.data  // Obtener la URI seleccionada

            if (selectedImageUri != null) {
                playlistImageView.setImageURI(selectedImageUri)
            }
        }
    }

    companion object {
        const val PICK_IMAGE_REQUEST = 1
    }
}