package com.example.soundnest_android.ui.playlists

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.DialogFragment
import com.example.soundnest_android.R

class NewPlaylistDialogFragment : DialogFragment() {

    var onPlaylistCreated: ((String, String, String?) -> Unit)? = null
    private lateinit var playlistImageView: ImageView
    private lateinit var placeholderLayout: View
    private var selectedImageUri: Uri? = null
    private var selectedImageBitmap: Bitmap? = null

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
        playlistImageView = view.findViewById(R.id.iv_playlist_image)
        placeholderLayout = view.findViewById(R.id.layoutImagePlaceholder)

        playlistImageView.scaleType = ImageView.ScaleType.CENTER_CROP

        val cardView =
            view.findViewById<androidx.cardview.widget.CardView>(R.id.cardViewPlaylistImage)
        cardView.setOnClickListener { pickImageLauncher.launch("image/*") }

        isCancelable = false

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
                name = it.getString(it.getColumnIndex(OpenableColumns.DISPLAY_NAME)) ?: ""
                size = it.getLong(it.getColumnIndex(OpenableColumns.SIZE))
            }
        }

        if (size > MAX_SIZE_BYTES) {
            Toast.makeText(requireContext(), "El archivo excede 20 MB", Toast.LENGTH_SHORT).show()
            return
        }

        val ext = name.substringAfterLast('.', "").lowercase()
        if (ext !in listOf("jpg", "jpeg", "png")) {
            Toast.makeText(requireContext(), "Solo PNG o JPG", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            resolver.openInputStream(uri)?.use { stream ->
                val bitmap = BitmapFactory.decodeStream(stream)
                if (bitmap != null) {
                    val resizedBitmap = resizeBitmap(bitmap, 300, 300)


                    playlistImageView.setImageDrawable(null)

                    playlistImageView.setImageBitmap(resizedBitmap)
                    playlistImageView.scaleType = ImageView.ScaleType.CENTER_CROP

                    try {
                        placeholderLayout.visibility = android.view.View.GONE
                        playlistImageView.visibility = android.view.View.VISIBLE
                    } catch (e: Exception) {
                        android.util.Log.e("NewPlaylist", "Error updating visibility", e)
                    }

                    playlistImageView.invalidate()
                    playlistImageView.requestLayout()

                    selectedImageUri = uri
                    selectedImageBitmap = resizedBitmap

                    if (bitmap != resizedBitmap) {
                        bitmap.recycle()
                    } else {

                    }
                } else {
                    Toast.makeText(
                        requireContext(),
                        "No se pudo cargar la imagen",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } catch (e: Exception) {
            Toast.makeText(
                requireContext(),
                "Error al cargar la imagen: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun resizeBitmap(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        if (width <= maxWidth && height <= maxHeight) {
            return bitmap
        }

        val scale = minOf(maxWidth.toFloat() / width, maxHeight.toFloat() / height)
        val newWidth = (width * scale).toInt()
        val newHeight = (height * scale).toInt()

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    override fun onDestroy() {
        super.onDestroy()
        selectedImageBitmap?.recycle()
        selectedImageBitmap = null
    }

    companion object {
        private const val MAX_SIZE_BYTES = 20 * 1024 * 1024
    }
}