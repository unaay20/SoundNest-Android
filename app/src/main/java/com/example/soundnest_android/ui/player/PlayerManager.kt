package com.example.soundnest_android.ui.player

import android.content.Context
import android.media.MediaPlayer
import androidx.annotation.RawRes

/**
 * Singleton manager to share a single MediaPlayer instance
 * between the PlayerControlFragment and SongInfoActivity.
 */
object  PlayerManager {
    private var mediaPlayer: MediaPlayer? = null

    /**
     * Initialize the MediaPlayer with a raw resource. Should be called
     * once in your MainActivity or Application before using.
     */
    fun init(context: Context, @RawRes resId: Int) {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(context.applicationContext, resId)
        }
    }

    /**
     * Get the shared MediaPlayer instance.
     */
    fun getPlayer(): MediaPlayer = mediaPlayer
        ?: throw IllegalStateException("PlayerManager not initialized. Call init() first.")

    /**
     * Toggle play/pause state.
     */
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

    /**
     * Release the player when no longer needed.
     */
    fun release() {
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
