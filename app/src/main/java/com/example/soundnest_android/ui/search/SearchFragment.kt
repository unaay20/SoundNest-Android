package com.example.soundnest_android.ui.search

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

    // Lista mutable de búsquedas recientes
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Ejemplos iniciales (puedes cargar desde SharedPreferences o base de datos)
        recentSearches.addAll(listOf(
            "android retrofit",
            "kotlin coroutines",
            "mvvm ejemplo"
        ))

        // Configurar el RecyclerView
        adapter = RecentSearchAdapter(recentSearches) { query ->
            // Al tocar un item, volver a ejecutar la búsqueda
            binding.searchView.setQuery(query, true)
        }
        binding.rvRecentSearches.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@SearchFragment.adapter
        }

        // Escuchar el SearchView
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                // Insertar al inicio y notificar al adapter
                recentSearches.add(0, query)
                adapter.notifyItemInserted(0)
                binding.rvRecentSearches.scrollToPosition(0)
                // Aquí lanzarías tu lógica real de búsqueda…
                return true
            }
            override fun onQueryTextChange(newText: String?): Boolean = false
        })

        binding.searchView.apply {
            // Asegúrate de que empieza iconificada:
            isIconified = true

            // Hacer toda la vista clickeable
            setOnClickListener {
                // Expande el SearchView
                isIconified = false
                // Opcional: fuerza el foco y muestra el teclado
                requestFocus()
                val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE)
                        as InputMethodManager
                imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
