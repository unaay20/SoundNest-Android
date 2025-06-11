package com.example.soundnest_android.ui.upload_song

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.example.soundnest_android.R
import com.example.soundnest_android.auth.SharedPrefsTokenProvider
import com.example.soundnest_android.databinding.ActivityUploadSongBinding
import com.example.soundnest_android.grpc.constants.GrpcRoutes
import com.example.soundnest_android.grpc.services.SongFileGrpcService
import com.example.soundnest_android.restful.constants.RestfulRoutes
import com.example.soundnest_android.restful.services.SongService

class UploadSongActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUploadSongBinding
    private lateinit var factory: UploadSongViewModelFactory
    private val viewModel: UploadSongViewModel by viewModels { factory }
    private var selectedImageUri: Uri? = null

    @SuppressLint("Range")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUploadSongBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val tokenProvider = SharedPrefsTokenProvider(this)
        val grpcService = SongFileGrpcService(
            GrpcRoutes.getHost(),
            GrpcRoutes.getPort(),
            { tokenProvider.getToken() }
        )
        val restService = SongService(RestfulRoutes.getBaseUrl(), tokenProvider)
        factory = UploadSongViewModelFactory(grpcService, restService, tokenProvider)

        viewModel.fetchGenres()
        viewModel.genres.observe(this, Observer { genres ->
            val realNames = genres?.map { it.genreName } ?: emptyList()
            val namesWithHint = listOf("Select a genre") + realNames
            val adapter =
                ArrayAdapter(this, android.R.layout.simple_spinner_item, namesWithHint).also {
                    it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                }
            binding.spinnerGenre.adapter = adapter
            binding.spinnerGenre.setSelection(0, false)
            binding.spinnerGenre.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>,
                        view: View?,
                        pos: Int,
                        id: Long
                    ) {
                        if (pos == 0) return
                        val selectedGenre = realNames[pos - 1]
                    }

                    override fun onNothingSelected(parent: AdapterView<*>) {}
                }
        })

        val pickFileLauncher = registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            uri?.let {
                viewModel.onFilePicked(it)
                val cursor = contentResolver.query(
                    it,
                    arrayOf(OpenableColumns.DISPLAY_NAME),
                    null, null, null
                )
                cursor?.use { c ->
                    if (c.moveToFirst()) {
                        val name = c.getString(c.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                        binding.tvFileName.text = name
                    }
                }
            }
        }

        val pickImageLauncher = registerForActivityResult(
            ActivityResultContracts.OpenDocument()
        ) { uri: Uri? ->
            uri?.let {
                selectedImageUri = it
                contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                val bitmap = contentResolver.openInputStream(it)?.use { stream ->
                    BitmapFactory.decodeStream(stream)
                }
                binding.ivSongImage.apply {
                    scaleType = ImageView.ScaleType.CENTER_CROP
                    setImageBitmap(bitmap)
                }
                viewModel.onImagePicked(it)
            }
        }

        binding.btnSelectFile.setOnClickListener {
            pickFileLauncher.launch("audio/*")
        }

        binding.btnSelectImage.setOnClickListener {
            pickImageLauncher.launch(arrayOf("image/jpeg", "image/png"))
        }

        binding.btnUpload.setOnClickListener {
            val name = binding.etSongName.text.toString()
            val desc = binding.etDescription.text.toString()
            val pos = binding.spinnerGenre.selectedItemPosition
            val genreId = viewModel.genres.value?.getOrNull(pos)?.idSongGenre ?: 0
            viewModel.onUploadClicked(this, name, desc, genreId)
        }

        viewModel.uploadSuccess.observe(this, Observer { success ->
            if (success) {
                Toast.makeText(this, getString(R.string.upload_success), Toast.LENGTH_SHORT).show()
                setResult(RESULT_OK)
                finish()
            }
        })

        viewModel.uploadError.observe(this, Observer { err ->
            err?.let { Toast.makeText(this, it, Toast.LENGTH_SHORT).show() }
        })
    }

    @SuppressLint("Range")
    private fun validateImage(uri: Uri): Boolean {
        val resolver = contentResolver
        val cursor = resolver.query(
            uri,
            arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE),
            null,
            null,
            null
        )
            ?: return false
        var name = ""
        var size = 0L
        cursor.use {
            if (it.moveToFirst()) {
                name = it.getString(it.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                size = it.getLong(it.getColumnIndex(OpenableColumns.SIZE))
            }
        }
        if (size > 20 * 1024 * 1024) {
            Toast.makeText(this, "El archivo excede 20 MB", Toast.LENGTH_SHORT).show()
            return false
        }
        val ext = name.substringAfterLast('.', "").lowercase()
        if (ext != "jpg" && ext != "jpeg" && ext != "png") {
            Toast.makeText(this, "Solo PNG o JPG", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }
}
