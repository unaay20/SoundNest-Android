package com.example.soundnest_android.ui.playlists

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.DialogFragment
import com.example.soundnest_android.R

class NewPlaylistDialogFragment : DialogFragment() {

    var onPlaylistCreated: ((String, String, String?) -> Unit)? = null
    private lateinit var playlistImageView: ImageView
    private var selectedImageUri: Uri? = null

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { handlePickedUri(it) }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity())
        val view = requireActivity().layoutInflater.inflate(R.layout.fragment_new_playlist, null)
        val nameEt = view.findViewById<EditText>(R.id.editTextPlaylistName)
        val descEt = view.findViewById<EditText>(R.id.editTextPlaylistDescription)
        playlistImageView = view.findViewById(R.id.editTextPlaylistImage)
        playlistImageView.setOnClickListener { pickImageLauncher.launch("image/*") }
        builder
            .setView(view)
            .setTitle(R.string.lbl_create_playlist)
            .setPositiveButton(R.string.btn_create) { _, _ ->
                onPlaylistCreated?.invoke(
                    nameEt.text.toString(),
                    descEt.text.toString(),
                    selectedImageUri?.toString()
                )
            }
            .setNegativeButton(R.string.btn_cancel) { dialog, _ -> dialog.dismiss() }
        return builder.create()
    }

    @SuppressLint("Range")
    private fun handlePickedUri(uri: Uri) {
        val resolver = requireContext().contentResolver
        val cursor = resolver.query(
            uri,
            arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE),
            null,
            null,
            null
        ) ?: return
        var name = ""
        var size = 0L
        cursor.use {
            if (it.moveToFirst()) {
                name = it.getString(it.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                size = it.getLong(it.getColumnIndex(OpenableColumns.SIZE))
            }
        }
        if (size > MAX_SIZE_BYTES) {
            Toast.makeText(requireContext(), "El archivo excede 20 MB", Toast.LENGTH_SHORT).show()
            return
        }
        val ext = name.substringAfterLast('.', "").lowercase()
        if (ext != "jpg" && ext != "jpeg" && ext != "png") {
            Toast.makeText(requireContext(), "Solo PNG o JPG", Toast.LENGTH_SHORT).show()
            return
        }
        resolver.openInputStream(uri)?.use { stream ->
            val bitmap = BitmapFactory.decodeStream(stream)
            playlistImageView.scaleType = ImageView.ScaleType.CENTER_CROP
            playlistImageView.setImageBitmap(bitmap)
            selectedImageUri = uri
        }
    }

    companion object {
        private const val MAX_SIZE_BYTES = 20 * 1024 * 1024
    }
}
