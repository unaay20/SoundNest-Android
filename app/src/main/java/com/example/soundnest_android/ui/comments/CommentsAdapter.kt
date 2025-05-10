package com.example.soundnest_android.ui.comments

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.example.soundnest_android.R
import com.example.soundnest_android.auth.SharedPrefsTokenProvider

class CommentsAdapter(private val comments: MutableList<Comment>) : RecyclerView.Adapter<CommentsAdapter.ViewHolder>() {

    var onDeleteComment: ((Comment) -> Unit)? = null
    var onReplyComment: ((Comment) -> Unit)? = null
    var isUserModerator: Boolean = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_comment, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val comment = comments[position]
        holder.tvAuthor.text = comment.user
        holder.tvText.text = comment.message

        holder.btnMoreOptions.setOnClickListener {
            val popupMenu = PopupMenu(holder.itemView.context, holder.btnMoreOptions)
            popupMenu.menu.add("Responder")
            if (comment.user == SharedPrefsTokenProvider(holder.itemView.context).username || isUserModerator) {
                popupMenu.menu.add("Eliminar")
            }

            popupMenu.setOnMenuItemClickListener { item ->
                when (item.title) {
                    "Eliminar" -> onDeleteComment?.invoke(comment)
                    "Responder" -> onReplyComment?.invoke(comment)
                }
                true
            }

            popupMenu.show()
        }
    }

    override fun getItemCount(): Int = comments.size

    fun setItems(newComments: List<Comment>) {
        comments.clear()
        comments.addAll(newComments)
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvAuthor: TextView = itemView.findViewById(R.id.tvCommentAuthor)
        val tvText: TextView = itemView.findViewById(R.id.tvCommentText)
        val btnMoreOptions: ImageButton = itemView.findViewById(R.id.btnMoreOptions)
    }
}
