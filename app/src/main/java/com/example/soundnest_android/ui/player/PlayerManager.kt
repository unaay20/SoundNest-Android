package com.example.soundnest_android.ui.player

import android.content.Context
import android.media.MediaPlayer
import androidx.annotation.RawRes

object  PlayerManager {
    private var mediaPlayer: MediaPlayer? = null

    fun init(context: Context, @RawRes resId: Int) {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(context.applicationContext, resId)
        }
    }

    fun initFromPath(path: String) {
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer().apply {
            setDataSource(path)
            prepare()
        }
    }

    fun getPlayer(): MediaPlayer = mediaPlayer
        ?: throw IllegalStateException("PlayerManager not initialized. Call init() first.")

    fun togglePlayPause(): Boolean {
        val player = getPlayer()
        return if (player.isPlaying) {
            player.pause()
            false
        } else {
            player.start()
            true
        }
    }

    fun release() {
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
