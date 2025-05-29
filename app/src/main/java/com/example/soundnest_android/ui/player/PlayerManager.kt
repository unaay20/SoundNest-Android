package com.example.soundnest_android.ui.player

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import java.io.File
import java.io.FileInputStream
import java.io.IOException

object PlayerManager {
    private var mediaPlayer: MediaPlayer? = null
    private var isPrepared: Boolean = false
    private var currentPlayingFile: File? = null

    interface PlayerStateListener {
        fun onTrackStarted()
        fun onTrackPaused()
        fun onTrackResumed()
        fun onTrackCompleted()
        fun onError()
    }

    var playerStateListener: PlayerStateListener? = null

    fun playFile(context: Context, file: File) {
        if (!file.exists()) {
            Log.e("PlayerManager", "File does not exist: ${file.absolutePath}")
            playerStateListener?.onError()
            return
        }

        if (mediaPlayer != null && file == currentPlayingFile && isPrepared) {
            if (!mediaPlayer!!.isPlaying) {
                mediaPlayer?.start()
                playerStateListener?.onTrackResumed()
            }
            return
        }

        releaseCurrentPlayer()

        currentPlayingFile = file
        isPrepared = false

        try {
            mediaPlayer = MediaPlayer().apply {
                val fis = FileInputStream(file)
                try {
                    setDataSource(fis.fd)
                } finally {
                    fis.close()
                }

                setOnPreparedListener { mp ->
                    isPrepared = true
                    mp.start()
                    playerStateListener?.onTrackStarted()
                    Log.d("PlayerManager", "MediaPlayer prepared and started.")
                }
                setOnCompletionListener {
                    isPrepared = false
                    playerStateListener?.onTrackCompleted()
                    Log.d("PlayerManager", "MediaPlayer playback completed.")
                }
                setOnErrorListener { mp, what, extra ->
                    Log.e("PlayerManager", "MediaPlayer Error - What: $what, Extra: $extra")
                    isPrepared = false
                    playerStateListener?.onError()
                    releaseCurrentPlayer()
                    true
                }
                prepareAsync()
                Log.d("PlayerManager", "MediaPlayer prepareAsync called.")
            }
        } catch (e: IOException) {
            Log.e("PlayerManager", "IOException during MediaPlayer setup: ${e.message}", e)
            playerStateListener?.onError()
            releaseCurrentPlayer()
        } catch (e: IllegalStateException) {
            Log.e(
                "PlayerManager",
                "IllegalStateException during MediaPlayer setup: ${e.message}",
                e
            )
            playerStateListener?.onError()
            releaseCurrentPlayer()
        } catch (e: Exception) {
            Log.e("PlayerManager", "Unexpected exception during MediaPlayer setup: ${e.message}", e)
            playerStateListener?.onError()
            releaseCurrentPlayer()
        }
    }

    fun togglePlayPause(): Boolean {
        mediaPlayer?.let {
            if (!isPrepared) {
                Log.w("PlayerManager", "togglePlayPause called when player not prepared.")
                return false
            }
            return if (it.isPlaying) {
                it.pause()
                playerStateListener?.onTrackPaused()
                Log.d("PlayerManager", "MediaPlayer paused.")
                false
            } else {
                it.start()
                playerStateListener?.onTrackResumed()
                Log.d("PlayerManager", "MediaPlayer resumed.")
                true
            }
        }
        Log.w("PlayerManager", "togglePlayPause called when mediaPlayer is null.")
        return false
    }

    fun getPlayer(): MediaPlayer? {
        return mediaPlayer
    }

    fun isPlaying(): Boolean {
        return mediaPlayer?.isPlaying == true && isPrepared
    }

    private fun releaseCurrentPlayer() {
        mediaPlayer?.stop()
        mediaPlayer?.reset()
        mediaPlayer?.release()
        mediaPlayer = null
        isPrepared = false
        currentPlayingFile = null
        Log.d("PlayerManager", "MediaPlayer released.")
    }

    fun release() {
        releaseCurrentPlayer()
        playerStateListener = null
    }
}