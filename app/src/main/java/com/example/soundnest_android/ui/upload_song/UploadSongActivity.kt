package com.example.soundnest_android.ui.upload_song


import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.soundnest_android.R
import com.example.soundnest_android.databinding.ActivityUploadSongBinding

class UploadSongActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUploadSongBinding
    private val viewModel: UploadSongViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUploadSongBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val genreAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.genres,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        binding.spinnerGenre.adapter = genreAdapter

        binding.btnSelectFile.setOnClickListener {
            viewModel.onSelectFile(this)
        }

        binding.btnUpload.setOnClickListener {
            viewModel.onUploadClicked(
                binding.etSongName.text.toString(),
                binding.etDescription.text.toString(),
                binding.spinnerGenre.selectedItem.toString()
            )
        }

        viewModel.fileUri.observe(this) { uri ->
            uri?.let {
                binding.btnSelectFile.text = it.lastPathSegment
            }
        }

        viewModel.uploadSuccess.observe(this) { success ->
            if (success) {
                Toast.makeText(this, getString(R.string.upload_success), Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == UploadSongViewModel.REQUEST_CODE_PICK_FILE && resultCode == Activity.RESULT_OK) {
            data?.data?.let { viewModel.onFilePicked(it) }
        }
    }
}