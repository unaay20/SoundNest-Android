package com.example.soundnest_android.ui.songs

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.example.soundnest_android.R
import com.example.soundnest_android.business_logic.Song
import java.time.Instant

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
        song = requireArguments().getSerializable(ARG_SONG) as Song
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SetTextI18n")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.fragment_detail_song_dialog, null)

        val imgCover = view.findViewById<ImageView>(R.id.imgCover)
        val tvTitle = view.findViewById<TextView>(R.id.tvTitle)
        val tvArtist = view.findViewById<TextView>(R.id.tvArtist)
        val tvDuration = view.findViewById<TextView>(R.id.tvDuration)
        val tvReleaseDate = view.findViewById<TextView>(R.id.tvReleaseDate)
        val tvDescription = view.findViewById<TextView>(R.id.tvDescription)
        val btnPlay = view.findViewById<Button>(R.id.btnPlay)
        val btnCancel = view.findViewById<Button>(R.id.btnCancel)

        tvTitle.text = song.title
        tvArtist.text = song.artist
        val minutes = song.duration / 60
        val seconds = song.duration % 60
        tvDuration.text = "Duration: %02d:%02d".format(minutes, seconds)


        val isoString = song.releaseDate
        val instant = Instant.parse(isoString)
        val releaseMillis = instant.toEpochMilli()

        val now = System.currentTimeMillis()
        tvReleaseDate.text = "Released ${
            DateUtils.getRelativeTimeSpanString(
                releaseMillis,
                now,
                DateUtils.MINUTE_IN_MILLIS,
                DateUtils.FORMAT_ABBREV_RELATIVE
            )
        }"

        val description = song.description?.takeIf { it.isNotBlank() } ?: "No description"
        tvDescription.text = "Description: $description"

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
    fun openSongInfo(
        song: Song,
        filePath: String?,
        playlist: List<Song>,
        index: Int
    )
}