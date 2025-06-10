package com.example.soundnest_android.ui.songs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.example.soundnest_android.R
import com.example.soundnest_android.business_logic.Song

class SongDialogFragment : DialogFragment() {

    companion object {
        private const val ARG_SONG = "ARG_SONG"

        fun newInstance(song: Song): SongDialogFragment {
            return SongDialogFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_SONG, song)
                }
            }
        }
    }

    private lateinit var song: Song

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        song = requireArguments()
            .getSerializable(ARG_SONG) as Song
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = requireActivity().layoutInflater
            .inflate(R.layout.fragment_detail_song_dialog, null)

        val imgCover = view.findViewById<ImageView>(R.id.imgCover)
        val tvTitle = view.findViewById<TextView>(R.id.tvTitle)
        val tvArtist = view.findViewById<TextView>(R.id.tvArtist)
        val btnPlay = view.findViewById<Button>(R.id.btnPlay)
        val btnCancel = view.findViewById<Button>(R.id.btnCancel)

        tvTitle.text = song.title
        tvArtist.text = song.artist

        song.coverUrl?.let { url ->
            Glide.with(this)
                .load(url)
                .into(imgCover)
        }

        btnPlay.setOnClickListener {
            (parentFragment as? PlayerHost ?: activity as? PlayerHost)
                ?.playSong(song)
            dismiss()
        }
        btnCancel.setOnClickListener { dismiss() }

        return AlertDialog.Builder(requireContext())
            .setView(view)
            .create()
    }
}

interface PlayerHost {
    fun playSong(song: Song)
    fun playNext()
    fun playPrevious()
}