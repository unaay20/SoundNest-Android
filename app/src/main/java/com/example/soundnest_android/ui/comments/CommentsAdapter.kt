package com.example.soundnest_android.ui.comments

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.soundnest_android.R
import com.example.soundnest_android.auth.SharedPrefsTokenProvider

class CommentsAdapter(
    private val comments: MutableList<Comment>
) : RecyclerView.Adapter<CommentsAdapter.ViewHolder>() {

    var onDeleteComment: ((Comment) -> Unit)? = null
    var onReplyComment: ((Comment) -> Unit)? = null
    var isUserModerator: Boolean = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_comment, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val comment = comments[position]
        holder.tvAuthor.text = comment.user
        holder.tvText.text = comment.message

        val isReply = comment.parentId != null
        val lp = holder.card.layoutParams as ViewGroup.MarginLayoutParams
        lp.marginStart = dpToPx(holder.card.context, if (isReply) 32 else 8)
        lp.topMargin   = dpToPx(holder.card.context, if (isReply) 4 else 8)
        holder.card.layoutParams = lp

        holder.btnMoreOptions.setOnClickListener {
            val popupMenu = PopupMenu(holder.itemView.context, holder.btnMoreOptions)
            popupMenu.menu.add(holder.itemView.context.getString(R.string.menu_reply))
            if (comment.user == SharedPrefsTokenProvider(holder.itemView.context).username
                || isUserModerator
            ) {
                popupMenu.menu.add(holder.itemView.context.getString(R.string.menu_delete))
            }
            popupMenu.setOnMenuItemClickListener { item ->
                when (item.title) {
                    holder.itemView.context.getString(R.string.menu_reply) -> onReplyComment?.invoke(comment)
                    holder.itemView.context.getString(R.string.menu_delete) -> onDeleteComment?.invoke(comment)
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

    private fun dpToPx(context: Context, dp: Int): Int {
        return (dp * context.resources.displayMetrics.density + 0.5f).toInt()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val card: CardView = itemView.findViewById(R.id.cardComment)
        val tvAuthor: TextView = itemView.findViewById(R.id.tvCommentAuthor)
        val tvText: TextView = itemView.findViewById(R.id.tvCommentText)
        val btnMoreOptions: ImageButton = itemView.findViewById(R.id.btnMoreOptions)
    }
}
