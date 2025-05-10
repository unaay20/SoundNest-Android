package com.example.soundnest_android.ui.comments

import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.getSystemService
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.soundnest_android.R
import com.example.soundnest_android.auth.SharedPrefsTokenProvider
import com.example.soundnest_android.ui.songs.Song
import com.bumptech.glide.Glide

class SongCommentsActivity : AppCompatActivity() {

    private val viewModel: SongCommentsViewModel by viewModels()
    private lateinit var commentsAdapter: CommentsAdapter
    private lateinit var song: Song

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_song_comments)

        song = intent.getSerializableExtra("EXTRA_SONG_OBJ") as? Song ?: run {
            Toast.makeText(this, "Canción no encontrada", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        viewModel.setSongId(song.id)

        val rvComments = findViewById<RecyclerView>(R.id.rvComments)
        val ivArtwork = findViewById<ImageView>(R.id.ivArtwork)
        val tvTitle = findViewById<TextView>(R.id.tvTitle)
        val tvArtist = findViewById<TextView>(R.id.tvArtist)
        val etNewComment = findViewById<EditText>(R.id.etNewComment)

        commentsAdapter = CommentsAdapter(mutableListOf())
        rvComments.apply {
            layoutManager = LinearLayoutManager(this@SongCommentsActivity)
            adapter = commentsAdapter
        }

        viewModel.comments.observe(this) { comments ->
            commentsAdapter.setItems(comments)
        }

        viewModel.error.observe(this) { errorMsg ->
            errorMsg?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.loadComments()

        tvTitle.text = song.title
        tvArtist.text = song.artist

        Glide.with(this)
            .load(song.coverResId)
            .into(ivArtwork)

        findViewById<Button>(R.id.btnSubmitComment).setOnClickListener {
            val commentText = etNewComment.text.toString()
            if (commentText.isNotBlank()) {
                val user = SharedPrefsTokenProvider(this).username
                if (user != null) {
                    viewModel.addComment(song.id, user, commentText)
                    etNewComment.text.clear()
                    hideKeyboard(etNewComment)
                } else {
                    Toast.makeText(this, "No se pudo obtener el usuario", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Por favor, escribe un comentario", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun hideKeyboard(view: EditText) {
        val imm = getSystemService<InputMethodManager>()
        imm?.hideSoftInputFromWindow(view.windowToken, 0)
    }
}
