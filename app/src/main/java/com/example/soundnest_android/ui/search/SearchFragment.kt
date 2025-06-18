package com.example.soundnest_android.ui.search

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.PopupWindow
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.soundnest_android.R
import com.example.soundnest_android.auth.SharedPrefsTokenProvider
import com.example.soundnest_android.business_logic.Song
import com.example.soundnest_android.databinding.FragmentSearchBinding
import com.example.soundnest_android.grpc.constants.GrpcRoutes
import com.example.soundnest_android.grpc.services.SongFileGrpcService
import com.example.soundnest_android.restful.constants.RestfulRoutes
import com.example.soundnest_android.restful.services.SongService
import com.example.soundnest_android.restful.services.VisitService
import com.example.soundnest_android.restful.utils.ApiResult
import com.example.soundnest_android.ui.player.SharedPlayerViewModel
import com.example.soundnest_android.ui.songs.SongDialogFragment
import kotlinx.coroutines.launch
import java.io.File

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SearchViewModel by viewModels()
    private val sharedPlayer: SharedPlayerViewModel by activityViewModels()

    private lateinit var adapter: RecentSearchAdapter
    private val fileName = "recent_searches.txt"
    private val recentSearches = mutableListOf<String>()
    private val songGrpc by lazy {
        SongFileGrpcService(
            GrpcRoutes.getHost(),
            GrpcRoutes.getPort()
        ) { SharedPrefsTokenProvider(requireContext()).getToken() }
    }
    private val visitService by lazy {
        VisitService(RestfulRoutes.getBaseUrl(), SharedPrefsTokenProvider(requireContext()))
    }
    private val songService by lazy {
        SongService(
            RestfulRoutes.getBaseUrl(),
            SharedPrefsTokenProvider(requireContext())
        )
    }

    private val playLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == AppCompatActivity.RESULT_OK) {
            val data = result.data ?: return@registerForActivityResult
            val song = data.getSerializableExtra("EXTRA_SONG_OBJ") as? Song
            val path = data.getStringExtra("EXTRA_FILE_PATH")
            if (song != null && path != null) {
                sharedPlayer.playFromFile(song, File(path))
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("RestrictedApi")
    @Suppress("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadFromFile()
        viewModel.loadRecent()

        adapter = RecentSearchAdapter(
            items = recentSearches,
            onClick = { q -> doSearch(q) },
            onDelete = { q ->
                adapter.remove(q)
                recentSearches.remove(q)
                saveToFile()
            }
        )
        binding.rvRecentSearches.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@SearchFragment.adapter
        }

        binding.btnClearAll.setOnClickListener {
            adapter.clearAll()
            recentSearches.clear()
            saveToFile()
        }

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                if (query.isBlank()) {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.msg_enter_text),
                        Toast.LENGTH_SHORT
                    ).show()
                    return false
                }
                doSearch(query)
                return true
            }

            override fun onQueryTextChange(newText: String?) = false
        })

        binding.searchView.apply {
            setIconifiedByDefault(true)
            isIconified = true

            val auto = findViewById<SearchView.SearchAutoComplete>(
                androidx.appcompat.R.id.search_src_text
            )
            auto.hint = getString(R.string.hint_type_search)
            auto.isFocusable = true
            auto.isFocusableInTouchMode = true

            setOnTouchListener { _, event ->
                if (isIconified && event.action == MotionEvent.ACTION_DOWN) {
                    isIconified = false
                    post {
                        auto.requestFocus()
                        val imm = requireContext()
                            .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.showSoftInput(auto, InputMethodManager.SHOW_IMPLICIT)
                    }
                    true
                } else false
            }

            setOnQueryTextFocusChangeListener { _, hasFocus ->
                if (!hasFocus) isIconified = true
            }
        }

        binding.btnFilter.setOnClickListener { showFilterPopup() }

        binding.fabRandomSong.setOnClickListener { showRandomSong() }
    }

    private fun doSearch(query: String) {
        adapter.addSearch(query)
        recentSearches.remove(query)
        recentSearches.add(0, query)
        saveToFile()

        playLauncher.launch(
            Intent(requireContext(), SearchResultActivity::class.java)
                .putExtra("QUERY", query)
        )
        binding.searchView.setQuery("", false)
        binding.searchView.clearFocus()
    }

    private fun showFilterPopup() {
        val popupView = layoutInflater.inflate(R.layout.fragment_filter, null, false)
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
        popup.showAsDropDown(binding.btnFilter)

        val etSong = popupView
            .findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etSongName)!!
        val etArtist =
            popupView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etArtistName)
        val spinner = popupView.findViewById<Spinner>(R.id.spinnerGenres)
        val btnApply = popupView.findViewById<Button>(R.id.btnApplyFilter)

        lifecycleScope.launch {
            when (val r = songService.getGenres()) {
                is ApiResult.Success -> {
                    val genres = r.data.orEmpty()
                    val names = genres.map { it.genreName }
                    spinner.adapter = ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_spinner_item,
                        names
                    ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
                }

                else -> Toast.makeText(
                    requireContext(),
                    getString(R.string.msg_error_loading_genres),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        btnApply.setOnClickListener {
            val songName = etSong.text.toString().takeIf { it.isNotBlank() }
            val artistName = etArtist.text.toString().takeIf { it.isNotBlank() }

            val genreId = viewModel.genres.value
                ?.getOrNull(spinner.selectedItemPosition)
                ?.genreName

            if (songName == null && artistName == null && genreId == null) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.msg_select_at_least_one_filter),
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            Intent(requireContext(), SearchResultActivity::class.java).apply {
                songName?.let { putExtra("FILTER_SONG", it) }
                artistName?.let { putExtra("FILTER_ARTIST", it) }
                genreId?.let { putExtra("FILTER_GENRE", it) }
                binding.searchView.query.toString()
                    .takeIf { it.isNotBlank() }
                    ?.let { putExtra("QUERY", it) }

                startActivity(this)
            }
            popup.dismiss()

        }

    }

    private fun showRandomSong() {
        lifecycleScope.launch {
            when (val r = songService.getRandom(1)) {
                is ApiResult.Success -> {
                    val resp = r.data.orEmpty().firstOrNull()
                    if (resp != null) {
                        val song = Song(
                            id = resp.idSong,
                            title = resp.songName.orEmpty(),
                            artist = resp.userName.orEmpty(),
                            coverUrl = resp.pathImageUrl?.let {
                                RestfulRoutes.getBaseUrl().removeSuffix("/") + it
                            },
                            duration = resp.durationSeconds,
                            releaseDate = resp.releaseDate.orEmpty(),
                            description = null
                        )
                        SongDialogFragment.newInstance(song)
                            .show(childFragmentManager, "dlgSong")
                    } else {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.msg_no_random_song),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                else -> Toast.makeText(
                    requireContext(),
                    getString(R.string.msg_error_getting_random_song),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun loadFromFile() {
        recentSearches.clear()
        val file = File(requireContext().filesDir, fileName)
        if (!file.exists()) file.createNewFile()
        file.bufferedReader().useLines { lines ->
            lines.map { it.trim() }
                .filter { it.isNotBlank() }
                .forEach { recentSearches.add(it) }
        }
    }

    private fun saveToFile() {
        Thread {
            try {
                val file = File(requireContext().filesDir, fileName)
                file.bufferedWriter().use { w ->
                    recentSearches.forEach {
                        w.write(it)
                        w.newLine()
                    }
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
