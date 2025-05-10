package com.example.soundnest_android.ui.comments

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.soundnest_android.R

class CommentsAdapter(
    private val items: MutableList<Comment>
) : RecyclerView.Adapter<CommentsAdapter.CommentViewHolder>() {

    inner class CommentViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        private val authorTv: TextView = v.findViewById(R.id.tvCommentAuthor)
        private val textTv: TextView = v.findViewById(R.id.tvCommentText)
        fun bind(c: Comment) {
            authorTv.text = c.user
            textTv.text = c.message
        }
    }

    override fun onCreateViewHolder(p: ViewGroup, viewType: Int) =
        CommentViewHolder(
            LayoutInflater.from(p.context)
                .inflate(R.layout.item_comment, p, false)
        )

    override fun onBindViewHolder(h: CommentViewHolder, pos: Int) =
        h.bind(items[pos])

    override fun getItemCount() = items.size

    fun setItems(newItems: List<Comment>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}
