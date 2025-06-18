package com.example.soundnest_android.ui.notifications

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.soundnest_android.R
import com.example.soundnest_android.restful.models.notification.NotificationResponse

class NotificationsAdapter(
    private val items: MutableList<NotificationResponse>,
    private val onClick: (NotificationResponse) -> Unit
) : RecyclerView.Adapter<NotificationsAdapter.NotificationViewHolder>() {

    inner class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tv: TextView = itemView.findViewById(R.id.tvNotificationText)
        private val container: View = itemView.findViewById(R.id.notificationContainer)

        init {
            itemView.setOnClickListener {
                val pos = adapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    onClick(items[pos])
                }
            }
        }

        fun bind(notification: NotificationResponse) {
            val ctx = itemView.context

            tv.text = notification.title?.takeIf { it.isNotBlank() }
                ?: ctx.getString(R.string.msg_new_notification)

            val lowText = ctx.getString(R.string.priority_low)
            val mediumText = ctx.getString(R.string.priority_medium)
            val highText = ctx.getString(R.string.priority_high)

            when (notification.relevance) {
                lowText -> container.setBackgroundColor(
                    ContextCompat.getColor(
                        itemView.context,
                        R.color.purple_200
                    )
                )

                mediumText -> container.setBackgroundColor(
                    ContextCompat.getColor(
                        itemView.context,
                        R.color.purple_500
                    )
                )

                highText -> container.setBackgroundColor(
                    ContextCompat.getColor(
                        itemView.context,
                        R.color.purple_700
                    )
                )
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun removeAt(position: Int) {
        items.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, items.size - position)
    }

    fun setItems(newItems: List<NotificationResponse>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}
