package com.example.soundnest_android.ui.search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.soundnest_android.R

class RecentSearchAdapter(
    private val items: MutableList<String>,
    private val onClick: (String) -> Unit,
    private val onDelete: (String) -> Unit
) : RecyclerView.Adapter<RecentSearchAdapter.SearchVH>() {

    inner class SearchVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tv: TextView = itemView.findViewById(R.id.tvRecentSearch)
        private val ivDelete: ImageView = itemView.findViewById(R.id.ivDelete)

        init {
            itemView.setOnClickListener {
                val pos = adapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    onClick(items[pos])
                }
            }
            ivDelete.setOnClickListener {
                val pos = adapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    onDelete(items[pos])
                }
            }
        }

        fun bind(text: String) {
            tv.text = text
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchVH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recent_search, parent, false)
        return SearchVH(view)
    }

    override fun onBindViewHolder(holder: SearchVH, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun addSearch(query: String) {
        val oldIndex = items.indexOf(query)
        if (oldIndex != -1) {
            items.removeAt(oldIndex)
            notifyItemRemoved(oldIndex)
        }
        items.add(0, query)
        notifyItemInserted(0)
    }

    fun remove(query: String) {
        val pos = items.indexOf(query)
        if (pos != -1) {
            items.removeAt(pos)
            notifyItemRemoved(pos)
        }
    }

    fun clearAll() {
        val count = items.size
        items.clear()
        notifyItemRangeRemoved(0, count)
    }
}

