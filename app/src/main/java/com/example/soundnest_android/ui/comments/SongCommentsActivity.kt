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
import com.example.soundnest_android.ui.songs.Song

class SongCommentsActivity : AppCompatActivity() {

    private val viewModel: SongCommentsViewModel by viewModels()
    private lateinit var commentsAdapter: CommentsAdapter
    private lateinit var song: Song

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_song_comments)

        // Obtener el objeto Song desde el intent
        song = intent.getSerializableExtra("EXTRA_SONG_OBJ") as? Song ?: run {
            Toast.makeText(this, "Canción no encontrada", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Referencias UI
        val tvTitle       = findViewById<TextView>(R.id.tvTitle)
        val tvArtist      = findViewById<TextView>(R.id.tvArtist)
        val ivArtwork     = findViewById<ImageView>(R.id.ivArtwork)
        val rvComments    = findViewById<RecyclerView>(R.id.rvComments)
        val etNewComment  = findViewById<EditText>(R.id.etNewComment)
        val btnSubmit     = findViewById<Button>(R.id.btnSubmitComment)

        // Mostrar información de la canción
        tvTitle.text = song.title
        tvArtist.text = song.artist
        ivArtwork.setImageResource(song.coverResId)

        // Configurar RecyclerView de comentarios
        commentsAdapter = CommentsAdapter(mutableListOf())
        rvComments.apply {
            layoutManager = LinearLayoutManager(this@SongCommentsActivity)
            adapter = commentsAdapter
        }

        // Observadores LiveData
        viewModel.comments.observe(this) { comments ->
            commentsAdapter.setItems(comments)
        }

        viewModel.error.observe(this) { errorMsg ->
            errorMsg?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        }

        // Cargar comentarios
        viewModel.loadComments(song.id)

        // Enviar comentario
        btnSubmit.setOnClickListener {
            val text = etNewComment.text.toString().trim()
            if (text.isNotEmpty()) {
                viewModel.addComment(
                    songId = song.id.toString(),
                    user = "UsuarioActual", // TODO: Reemplaza con el usuario real
                    text = text
                )
                etNewComment.text.clear()
                etNewComment.clearFocus()
                hideKeyboard(etNewComment)
            } else {
                Toast.makeText(this, "Escribe un comentario", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Utilidad: ocultar teclado
    private fun hideKeyboard(view: EditText) {
        val imm = getSystemService<InputMethodManager>()
        imm?.hideSoftInputFromWindow(view.windowToken, 0)
    }
}
