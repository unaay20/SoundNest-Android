package com.example.soundnest_android.ui.search

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.PopupWindow
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.soundnest_android.R
import com.example.soundnest_android.auth.SharedPrefsTokenProvider
import com.example.soundnest_android.business_logic.Song
import com.example.soundnest_android.databinding.FragmentSearchBinding
import com.example.soundnest_android.grpc.constants.GrpcRoutes
import com.example.soundnest_android.grpc.http.GrpcResult
import com.example.soundnest_android.grpc.services.SongFileGrpcService
import com.example.soundnest_android.restful.constants.RestfulRoutes
import com.example.soundnest_android.restful.models.song.GenreResponse
import com.example.soundnest_android.restful.services.SongService
import com.example.soundnest_android.restful.utils.ApiResult
import com.example.soundnest_android.ui.player.SharedPlayerViewModel
import com.example.soundnest_android.ui.songs.PlayerHost
import com.example.soundnest_android.ui.songs.SongDialogFragment
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class SearchFragment : Fragment(), PlayerHost {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: RecentSearchAdapter
    private val fileName = "recent_searches.txt"
    private val recentSearches = mutableListOf<String>()
    private var genresList: List<GenreResponse> = emptyList()

    private val songService: SongService by lazy {
        SongService(
            RestfulRoutes.getBaseUrl(),
            SharedPrefsTokenProvider(requireContext())
        )
    }

    private val sharedPlayer: SharedPlayerViewModel by activityViewModels()
    private var isFirstSongEverPlayed: Boolean = true
    private val songGrpc by lazy {
        SongFileGrpcService(
            GrpcRoutes.getHost(),
            GrpcRoutes.getPort()
        ) { SharedPrefsTokenProvider(requireContext()).getToken() }
    }

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
                adapter.remove(query)
                saveToFile()
            }
        )


        binding.rvRecentSearches.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@SearchFragment.adapter
        }

        binding.btnClearAll.setOnClickListener {
            adapter.clearAll()
            saveToFile()
        }

        binding.btnFilter.setOnClickListener { showFilterPopup() }

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

        binding.fabRandomSong.setOnClickListener {
            lifecycleScope.launch {
                when (val r = songService.getRandom(1)) {
                    is ApiResult.Success -> {
                        val list = r.data.orEmpty()
                        if (list.isNotEmpty()) {
                            val resp = list.first()
                            val song = Song(
                                id = resp.idSong,
                                title = resp.songName.orEmpty(),
                                artist = resp.userName.orEmpty(),
                                coverUrl = resp.pathImageUrl?.let { base ->
                                    RestfulRoutes.getBaseUrl().removeSuffix("/") + base
                                }
                            )
                            showSongFragment(song)
                        } else {
                            Toast.makeText(
                                requireContext(),
                                "No hay canción aleatoria",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    else -> {
                        Toast.makeText(
                            requireContext(),
                            "Error al obtener canción aleatoria",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
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

    private fun showSongFragment(song: Song) {
        SongDialogFragment.newInstance(song)
            .show(childFragmentManager, "dlgSong")
    }

    override fun playSong(song: Song) {
        downloadAndQueue(song)
    }

    private fun downloadAndQueue(song: Song) {
        val cacheFile = File(requireContext().cacheDir, "song_${song.id}.mp3")
        if (cacheFile.exists()) {
            sharedPlayer.playFromFile(song, cacheFile)
            return
        }
        sharedPlayer.setLoading(true)
        lifecycleScope.launch(Dispatchers.IO) {
            val tmpFile = File(requireContext().cacheDir, "song_${song.id}.tmp")
            if (tmpFile.exists()) tmpFile.delete()
            when (val res = songGrpc.downloadSongStreamTo(song.id, tmpFile.outputStream())) {
                is GrpcResult.Success -> tmpFile.renameTo(cacheFile)
                else -> tmpFile.delete()
            }
            withContext(Dispatchers.Main) {
                sharedPlayer.setLoading(false)
                if (isFirstSongEverPlayed) {
                    Log.d(
                        "HomeFragment",
                        "Attempting to play FIRST song directly: ${cacheFile.name}"
                    )
                    sharedPlayer.playFromFile(song, cacheFile)
                    isFirstSongEverPlayed = false
                } else {
                    sharedPlayer.playFromFile(song, cacheFile)
                }
            }
        }
    }

    private fun showFilterPopup() {
        val popupView = layoutInflater.inflate(R.layout.fragment_filter, null)
        val popup = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        ).apply {
            elevation = 10f
            isOutsideTouchable = true
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
        popup.showAsDropDown(binding.btnFilter, 0, 0)

        val etSong = popupView.findViewById<TextInputEditText>(R.id.etSongName)
        val etArtist = popupView.findViewById<TextInputEditText>(R.id.etArtistName)
        val spinner = popupView.findViewById<Spinner>(R.id.spinnerGenres)
        val btnApply = popupView.findViewById<Button>(R.id.btnApplyFilter)

        lifecycleScope.launch {
            when (val res = songService.getGenres()) {
                is ApiResult.Success -> {
                    genresList = res.data.orEmpty()
                    val names = genresList.map { it.genreName }
                    spinner.adapter = ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_spinner_item,
                        names
                    ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
                }

                else -> Toast.makeText(
                    requireContext(),
                    "Error al cargar géneros",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        btnApply.setOnClickListener {
            val songName = etSong.text?.toString().takeIf { !it.isNullOrBlank() }
            val artistName = etArtist.text?.toString().takeIf { !it.isNullOrBlank() }
            val genrePos = spinner.selectedItemPosition

            val genreId = genresList.getOrNull(genrePos)?.genreName

            Intent(requireContext(), SearchResultActivity::class.java).also {
                songName?.let { q -> it.putExtra("FILTER_SONG", q) }
                artistName?.let { a -> it.putExtra("FILTER_ARTIST", a) }
                genreId?.let { g -> it.putExtra("FILTER_GENRE", g) }
                startActivity(it)
            }
            popup.dismiss()
        }

        popup.showAsDropDown(binding.btnFilter, 0, 0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
