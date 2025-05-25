package com.example.soundnest_android.ui.search

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.widget.SearchView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.soundnest_android.databinding.FragmentSearchBinding
import java.io.File

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: RecentSearchAdapter
    private val fileName = "recent_searches.txt"
    private val recentSearches = mutableListOf<String>()

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

        loadFromFile()

        adapter = RecentSearchAdapter(recentSearches,
            onClick = { query -> doSearch(query) },
            onDelete = { query ->
                recentSearches.remove(query)
                adapter.remove(query)
                saveToFile()
            }
        )


        binding.rvRecentSearches.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@SearchFragment.adapter
        }

        binding.btnClearAll.setOnClickListener {
            recentSearches.clear()
            adapter.clearAll()
            saveToFile()
        }

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                doSearch(query)
                return true
            }
            override fun onQueryTextChange(newText: String?) = false
        })


        binding.searchView.apply {
            setIconifiedByDefault(true)
            isIconified = true

            val searchAutoComplete = binding.searchView
                .findViewById<SearchView.SearchAutoComplete>(
                    androidx.appcompat.R.id.search_src_text
                )

            searchAutoComplete.hint = "Type your search here..."
            searchAutoComplete.isFocusable = true
            searchAutoComplete.isFocusableInTouchMode = true

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

            setOnQueryTextFocusChangeListener { _, hasFocus ->
                if (!hasFocus) isIconified = true
            }
        }

    }

    private fun loadFromFile() {
        recentSearches.clear()
        val file = File(requireContext().filesDir, fileName)

        if (!file.exists()) {
            file.createNewFile()
        }

        file.bufferedReader().useLines { lines ->
            lines
                .map { it.trim() }
                .filter { it.isNotBlank() }
                .forEach { recentSearches.add(it) }
        }
    }

    private fun doSearch(query: String) {
        adapter.addSearch(query)
        saveToFile()

        val intent = Intent(requireContext(), SearchResultActivity::class.java)
            .putExtra("QUERY", query)
        startActivity(intent)

        binding.searchView.setQuery("", false)
        binding.searchView.clearFocus()
    }

    private fun saveToFile() {
        Thread {
            try {
                val file = File(requireContext().filesDir, fileName)
                file.bufferedWriter().use { w ->
                    recentSearches.forEach { w.write(it); w.newLine() }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
