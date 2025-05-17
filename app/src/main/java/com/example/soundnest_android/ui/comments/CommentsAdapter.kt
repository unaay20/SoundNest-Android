package com.example.soundnest_android.ui.comments

import android.content.Context
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.soundnest_android.R
import com.example.soundnest_android.auth.SharedPrefsTokenProvider
import com.example.soundnest_android.business_logic.Comment

class CommentsAdapter : RecyclerView.Adapter<CommentsAdapter.ViewHolder>() {

    private val items = mutableListOf<Pair<Comment, Int>>()

    var onDeleteComment: ((Comment) -> Unit)? = null
    var onReplyComment: ((Comment) -> Unit)? = null

    var isUserModerator: Boolean = false

    fun setItems(comments: List<Comment>) {
        items.clear()
        items.addAll(flatten(comments))
        notifyDataSetChanged()
    }

    private fun flatten(list: List<Comment>, depth: Int = 0): List<Pair<Comment, Int>> {
        return list.flatMap { c ->
            listOf(c to depth) + flatten(c.responses, depth + 1)
        }
    }

    override fun getItemCount(): Int = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_comment, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (comment, depth) = items[position]

        holder.tvAuthor.text = comment.user
        holder.tvText.text = comment.message

        val lp = holder.card.layoutParams as ViewGroup.MarginLayoutParams
        lp.marginStart = dpToPx(holder.card.context, 8 + depth * 24)
        lp.topMargin = dpToPx(holder.card.context, if (depth > 0) 4 else 8)
        holder.card.layoutParams = lp

        holder.tvText.setTextSize(
            TypedValue.COMPLEX_UNIT_SP,
            if (depth == 0) 16f else 14f
        )

        holder.btnMoreOptions.setOnClickListener { view ->
            val popup = PopupMenu(view.context, view)
            popup.menu.add(R.string.menu_reply)
            val currentUser = SharedPrefsTokenProvider(view.context).username
            if (comment.user == currentUser || isUserModerator) {
                popup.menu.add(R.string.menu_delete)
            }
            popup.setOnMenuItemClickListener { item ->
                when (item.title) {
                    view.context.getString(R.string.menu_reply) -> onReplyComment?.invoke(comment)
                    view.context.getString(R.string.menu_delete) -> onDeleteComment?.invoke(comment)
                }
                true
            }
            popup.show()
        }
    }

    private fun dpToPx(context: Context, dp: Int): Int =
        (dp * context.resources.displayMetrics.density + 0.5f).toInt()

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val card: CardView = itemView.findViewById(R.id.cardComment)
        val tvAuthor: TextView = itemView.findViewById(R.id.tvCommentAuthor)
        val tvText: TextView = itemView.findViewById(R.id.tvCommentText)
        val btnMoreOptions: ImageButton = itemView.findViewById(R.id.btnMoreOptions)
    }
}
