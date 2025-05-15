package com.example.soundnest_android.ui.player

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.soundnest_android.R

class PlayerControlFragment : Fragment(R.layout.fragment_player_control) {

    private var isPlaying = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val songImage   = view.findViewById<ImageView>(R.id.songImage)
        val songTitle   = view.findViewById<TextView>(R.id.songTitle)
        val artistName  = view.findViewById<TextView>(R.id.artistName)

        val btnBack      = view.findViewById<ImageButton>(R.id.btnBack)
        val btnPlayPause = view.findViewById<ImageButton>(R.id.btnPlayPause)
        val btnNext      = view.findViewById<ImageButton>(R.id.btnNext)
        val btnDownload  = view.findViewById<ImageButton>(R.id.btnDownload)
        val btnComments  = view.findViewById<ImageButton>(R.id.btnComments)

        songImage.setImageResource(R.drawable.img_default_song)
        songTitle.text  = "19 días y 500 noches"
        artistName.text = "Joaquín Sabina"

        view.setOnClickListener {
            Intent(requireContext(), SongInfoActivity::class.java).also { intent ->
                intent.putExtra("EXTRA_TITLE", songTitle.text.toString())
                intent.putExtra("EXTRA_ARTIST", artistName.text.toString())
                intent.putExtra("EXTRA_IMAGE_RES", R.drawable.img_default_song)
                startActivity(intent)
            }
        }

        btnPlayPause.setOnClickListener {
            val isPlaying = PlayerManager.togglePlayPause()
            btnPlayPause.setImageResource(
                if (isPlaying) R.drawable.ic_baseline_pause else R.drawable.ic_baseline_play
            )
        }
        btnBack.setOnClickListener { /* lógica de retroceso */ }
        btnNext.setOnClickListener { /* lógica de siguiente */ }

        btnDownload.setOnClickListener { /* lógica de descarga */ }
        btnComments.setOnClickListener { /* lógica de comentarios */ }
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}
