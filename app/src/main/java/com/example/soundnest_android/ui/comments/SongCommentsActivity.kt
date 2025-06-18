package com.example.soundnest_android.ui.comments

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.getSystemService
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.soundnest_android.R
import com.example.soundnest_android.auth.SharedPrefsTokenProvider
import com.example.soundnest_android.business_logic.Comment
import com.example.soundnest_android.business_logic.Song

class SongCommentsActivity : AppCompatActivity() {

    private val viewModel: SongCommentsViewModel by viewModels {
        SongCommentsViewModelFactory(
            application,
            SharedPrefsTokenProvider(this)
        )
    }
    private lateinit var commentsAdapter: CommentsAdapter
    private lateinit var song: Song

    private var parentComment: Comment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN or
                    WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        )
        setContentView(R.layout.activity_song_comments)

        val tvNoComments = findViewById<TextView>(R.id.tvNoComments)
        val rvComments = findViewById<RecyclerView>(R.id.rvComments)
        val inputRow = findViewById<LinearLayout>(R.id.comment_input_row)
        val ivArtwork = findViewById<ImageView>(R.id.ivArtwork)
        val tvTitle = findViewById<TextView>(R.id.tvTitle)
        val tvArtist = findViewById<TextView>(R.id.tvArtist)
        val etNewComment = findViewById<EditText>(R.id.etNewComment)
        val btnSubmit = findViewById<Button>(R.id.btnSubmitComment)
        val btnCancel = findViewById<Button>(R.id.btnCancelComment)

        song = intent.getSerializableExtra("EXTRA_SONG_OBJ") as? Song ?: run {
            Toast.makeText(this, getString(R.string.msg_song_not_found), Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        viewModel.setSongId(song.id)

        commentsAdapter = CommentsAdapter().apply {
            onDeleteComment = { comment ->
                val message = getString(R.string.msg_delete_comment, comment.user)
                AlertDialog.Builder(this@SongCommentsActivity)
                    .setTitle(R.string.lbl_delete_comment)
                    .setMessage(message)
                    .setPositiveButton(R.string.btn_delete) { _, _ ->
                        viewModel.deleteComment(comment.id)
                    }
                    .setNegativeButton(R.string.btn_cancel, null)
                    .show()
            }
            onReplyComment = { comment ->
                parentComment = comment
                val hint = getString(R.string.hint_reply_to)
                etNewComment.hint = hint + comment.user
                btnSubmit.text = getString(R.string.btn_send)
                btnCancel.visibility = Button.VISIBLE
            }
            isUserModerator = /* tu lógica para moderador */ false
        }

        rvComments.apply {
            layoutManager = LinearLayoutManager(this@SongCommentsActivity)
            adapter = commentsAdapter
        }

        viewModel.comments.observe(this) { comments ->
            commentsAdapter.setItems(comments)
            if (comments.isEmpty()) {
                Toast.makeText(this, R.string.lbl_no_comments, Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.comments.observe(this) { comments ->
            if (comments.isNotEmpty()) {
                tvNoComments.visibility = View.GONE
                rvComments.visibility = View.VISIBLE
                inputRow.visibility = View.VISIBLE

                commentsAdapter.setItems(comments)
            }
        }

        viewModel.error.observe(this) { errorMsg ->
            if (errorMsg != null) {
                if (errorMsg.contains("404")) {
                    tvNoComments.visibility = View.VISIBLE
                    rvComments.visibility = View.GONE
                    inputRow.visibility = View.VISIBLE
                } else {
                    val msg = getString(R.string.error_loading_comments)
                    Toast.makeText(this, "$msg: $errorMsg", Toast.LENGTH_LONG).show()
                }
            }
        }
        viewModel.loadComments()

        tvTitle.text = song.title
        tvArtist.text = song.artist
        Glide.with(this).load(song.coverUrl).into(ivArtwork)

        btnCancel.setOnClickListener {
            parentComment = null
            etNewComment.hint = getString(R.string.hint_new_comment)
            btnSubmit.text = getString(R.string.btn_send)
            btnCancel.visibility = Button.GONE
        }

        btnSubmit.setOnClickListener {
            val text = etNewComment.text.toString().trim()
            if (text.isBlank()) {
                val message = getString(R.string.error_empty_comment)
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val provider = SharedPrefsTokenProvider(this)
            val user = provider.username
            Log.d("SongCommentsActivity", "Submit by user: $user, token: ${provider.getToken()}")
            if (user == null) {
                val message = getString(R.string.error_no_user)
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            parentComment?.let { parent ->
                viewModel.replyToComment(parent.id, text)
            } ?: run {
                viewModel.addComment(song.id, user, text)
            }

            etNewComment.text.clear()
            btnCancel.visibility = Button.GONE
            parentComment = null
            etNewComment.hint = getString(R.string.hint_new_comment)
            btnSubmit.text = getString(R.string.btn_send)
            hideKeyboard(etNewComment)
        }
    }

    private fun hideKeyboard(view: EditText) {
        val imm = getSystemService<InputMethodManager>()
        imm?.hideSoftInputFromWindow(view.windowToken, 0)
    }
}
