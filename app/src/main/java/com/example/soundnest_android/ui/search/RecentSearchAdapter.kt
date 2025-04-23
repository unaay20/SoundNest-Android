package com.example.soundnest_android.ui.search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.soundnest_android.R

class RecentSearchAdapter(
    private val items: List<String>,
    private val onClick: (String) -> Unit
) : RecyclerView.Adapter<RecentSearchAdapter.SearchVH>() {

    inner class SearchVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tv: TextView = itemView.findViewById(R.id.tvRecentSearch)

        init {
            itemView.setOnClickListener {
                // obtener posición segura
                val pos = adapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    onClick(items[pos])
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
}
