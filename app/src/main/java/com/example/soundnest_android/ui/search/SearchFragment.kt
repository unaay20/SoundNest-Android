package com.example.soundnest_android.ui.search

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.soundnest_android.databinding.FragmentSearchBinding

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private val recentSearches = mutableListOf<String>()
    private lateinit var adapter: RecentSearchAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("RestrictedApi", "ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recentSearches.addAll(listOf(
            "android retrofit",
            "kotlin coroutines",
            "mvvm ejemplo"
        ))

        adapter = RecentSearchAdapter(recentSearches) { query ->
            binding.searchView.setQuery(query, true)
            adapter.addSearch(query)
        }

        binding.rvRecentSearches.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@SearchFragment.adapter
        }

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                adapter.addSearch(query)
                binding.searchView.setQuery("", false)
                return true
            }
            override fun onQueryTextChange(newText: String?): Boolean = false
        })

        binding.searchView.apply {
            setIconifiedByDefault(true)
            isIconified = true

            val searchAutoComplete = findViewById<SearchView.SearchAutoComplete>(
                androidx.appcompat.R.id.search_src_text
            ).apply {
                isFocusable = true
                isFocusableInTouchMode = true
            }

            setOnTouchListener { _, event ->
                if (isIconified) {
                    isIconified = false
                    post {
                        searchAutoComplete.requestFocus()
                        val imm = requireContext().getSystemService(
                            Context.INPUT_METHOD_SERVICE
                        ) as InputMethodManager
                        imm.showSoftInput(
                            searchAutoComplete,
                            InputMethodManager.SHOW_IMPLICIT
                        )
                    }
                    return@setOnTouchListener true
                }
                false
            }

            // 4) Si pierde foco, opcionalmente lo cierras de nuevo
            setOnQueryTextFocusChangeListener { _, hasFocus ->
                if (!hasFocus) isIconified = true
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
